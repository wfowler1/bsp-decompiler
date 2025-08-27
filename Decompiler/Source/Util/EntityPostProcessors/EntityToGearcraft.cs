using System;
using System.Collections.Generic;
using System.Numerics;

using LibBSP;

namespace Decompiler
{
    /// <summary>
    /// Class containing methods for parsing entities from various BSP formats into those for Gearcraft.
    /// </summary>
    public class EntityToGearcraft
    {

        private Job _master;

        private Entities _entities;
        private MapType _version;

        private bool _isCTF;

        /// <summary>
        /// Creates a new instance of an <see cref="EntityToGrarcraft"/> object which will operate on the passed <see cref="Entities"/>.
        /// </summary>
        /// <param name="entities">The <see cref="Entities"/> to postprocess.</param>
        /// <param name="version">The <see cref="MapType"/> of the BSP the entities are from.</param>
        /// <param name="master">The parent <see cref="Job"/> object for this instance.</param>
        public EntityToGearcraft(Entities entities, MapType version, Job master)
        {
            _entities = entities;
            _version = version;
            _master = master;
        }

        /// <summary>
        /// Processes every <see cref="Entity"/> in an <see cref="Entities"/> object to be used in a Gearcraft map.
        /// </summary>
        public void PostProcessEntities()
        {
            // There should really only be one of these. But someone might have screwed with the map...
            List<Entity> worldspawns = _entities.FindAll(entity => { return entity.ClassName.Equals("worldspawn", StringComparison.InvariantCultureIgnoreCase); });
            foreach (Entity entity in worldspawns)
            {
                entity["mapversion"] = "510";
            }

            // Detect and parse water
            Entity waterEntitiy = PostProcessWater(worldspawns);
            if (waterEntitiy != null)
            {
                _entities.Add(waterEntitiy);
            }

            // Detect and parse lava
            Entity lavaEntitiy = PostProcessLava(worldspawns);
            if (lavaEntitiy != null)
            {
                _entities.Add(lavaEntitiy);
            }

            // We might modify the collection as we iterate over it. Can't use foreach.
            for (int i = 0; i < _entities.Count; ++i)
            {
                if (!_master.settings.noEntCorrection)
                {
                    PostProcessEntity(_entities[i]);
                }
                if (!_master.settings.noTexCorrection)
                {
                    PostProcessTextures(_entities[i].brushes);
                }
            }

            if (_isCTF)
            {
                foreach (Entity entity in worldspawns)
                {
                    entity["defaultctf"] = "1";
                }
            }
        }

        /// <summary>
        /// Moves any <see cref="MAPBrush"/> with <c>isWater</c> <c>true</c> from the passed <see cref="Entity"/> objects into a
        /// new <see cref="Entity"/> object and returns it.
        /// </summary>
        /// <param name="entities">An enumerable object of <see cref="Entity"/> objects to strip water <see cref="MAPBrush"/> objects from.</param>
        /// <returns>A new <see cref="Entity"/> object containing all the stripped water <see cref="MAPBrush"/> objects.</returns>
        private static Entity PostProcessWater(IEnumerable<Entity> entities)
        {
            Entity waterEntity = null;
            foreach (Entity entity in entities)
            {
                for (int i = 0; i < entity.brushes.Count; ++i)
                {
                    if (entity.brushes[i].isWater)
                    {
                        // Don't create a new entity for the water unless we need it, but only create one
                        if (waterEntity == null)
                        {
                            waterEntity = CreateNewWaterEntity();
                        }
                        waterEntity.brushes.Add(entity.brushes[i]);
                        entity.brushes.RemoveAt(i);
                        --i;
                    }
                }
            }

            return waterEntity;
        }

        /// <summary>
        /// Moves any <see cref="MAPBrush"/> with <c>isLava</c> <c>true</c> from the passed <see cref="Entity"/> objects into a
        /// new <see cref="Entity"/> object and returns it.
        /// </summary>
        /// <param name="entities">An enumerable object of <see cref="Entity"/> objects to strip lava <see cref="MAPBrush"/> objects from.</param>
        /// <returns>A new <see cref="Entity"/> object containing all the stripped lava <see cref="MAPBrush"/> objects.</returns>
        private static Entity PostProcessLava(IEnumerable<Entity> entities)
        {
            Entity waterEntity = null;
            foreach (Entity entity in entities)
            {
                for (int i = 0; i < entity.brushes.Count; ++i)
                {
                    if (entity.brushes[i].isLava)
                    {
                        // Don't create a new entity for the water unless we need it, but only create one
                        if (waterEntity == null)
                        {
                            waterEntity = CreateNewWaterEntity();
                        }
                        waterEntity["skin"] = "-4";
                        waterEntity["rendercolor"] = "255 128 0";
                        waterEntity["renderamt"] = "255";
                        waterEntity["rendermode"] = "5";
                        waterEntity["renderfx"] = "14";
                        waterEntity["WaveHeight"] = "1";
                        waterEntity.brushes.Add(entity.brushes[i]);
                        entity.brushes.RemoveAt(i);
                        --i;
                    }
                }
            }

            return waterEntity;
        }

        /// <summary>
        /// Creates a new empty water <see cref="Entity"/>.
        /// </summary>
        /// <returns>A new empty water <see cref="Entity"/> with good defaults.</returns>
        private static Entity CreateNewWaterEntity()
        {
            Entity waterEntity = new Entity("func_water");
            waterEntity["rendercolor"] = "0 0 0";
            waterEntity["speed"] = "100";
            waterEntity["wait"] = "4";
            waterEntity["skin"] = "-3";
            waterEntity["WaveHeight"] = "3.2";
            return waterEntity;
        }

        /// <summary>
        /// Sends <paramref name="entity"/> to be postprocessed into the appropriate method based on version.
        /// </summary>
        /// <param name="entity"><see cref="Entity"/> to postprocess.</param>
        private void PostProcessEntity(Entity entity)
        {
            if (_version == MapType.Nightfire)
            {
                PostProcessNightfireEntity(entity);
            }
            else if (_version.IsSubtypeOf(MapType.CoD))
            {
                PostProcessCoDEntity(entity);
            }
            else if (_version.IsSubtypeOf(MapType.Quake2))
            {
                PostProcessQuake2Entity(entity);
            }
            else if (_version.IsSubtypeOf(MapType.Quake3))
            {
                PostProcessQuake3Entity(entity);
            }
            else if (_version.IsSubtypeOf(MapType.Source))
            {
                PostProcessSourceEntity(entity);
            }
        }

        /// <summary>
        /// Postprocesser to convert an <see cref="Entity"/> from a Nightfire BSP to one for Gearcraft.
        /// </summary>
        /// <param name="entity">The <see cref="Entity"/> to parse.</param>
        private void PostProcessNightfireEntity(Entity entity)
        {
            if (entity.IsBrushBased)
            {
                Vector3 origin = entity.Origin;
                entity.Remove("origin");
                entity.Remove("model");
                if (origin != Vector3.Zero)
                {
                    // If this brush has an origin
                    MAPBrush neworiginBrush = MAPBrushExtensions.CreateCube(new Vector3(-16, -16, -16), new Vector3(16, 16, 16), "special/origin");
                    entity.brushes.Add(neworiginBrush);
                }
                foreach (MAPBrush brush in entity.brushes)
                {
                    brush.Translate(origin);
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert an <see cref="Entity"/> from a Source engine BSP to one for Gearcraft.
        /// </summary>
        /// <param name="entity">The <see cref="Entity"/> to parse.</param>
        private void PostProcessSourceEntity(Entity entity)
        {
            entity.Remove("hammerid");
            if (entity.IsBrushBased)
            {
                Vector3 origin = entity.Origin;
                entity.Remove("origin");
                entity.Remove("model");
                if (entity.ValueIs("classname", "func_door_rotating"))
                {
                    // TODO: What entities require origin brushes?
                    if (origin != Vector3.Zero)
                    {
                        MAPBrush neworiginBrush = MAPBrushExtensions.CreateCube(new Vector3(-16, -16, -16), new Vector3(16, 16, 16), "special/origin");
                        entity.brushes.Add(neworiginBrush);
                    }
                }
                foreach (MAPBrush brush in entity.brushes)
                {
                    brush.Translate(origin);
                }
            }

            switch (entity["classname"].ToLower())
            {
                case "func_breakable_surf":
                {
                    entity["classname"] = "func_breakable";
                    break;
                }
                case "func_brush":
                {
                    if (entity["solidity"] == "0")
                    {
                        entity["classname"] = "func_wall_toggle";
                        if (entity["StartDisabled"] == "1")
                        {
                            entity["spawnflags"] = "1";
                        }
                        else
                        {
                            entity["spawnflags"] = "0";
                        }
                        entity.Remove("StartDisabled");
                    }
                    else
                    {
                        if (entity["solidity"] == "1")
                        {
                            entity["classname"] = "func_illusionary";
                        }
                        else
                        {
                            entity["classname"] = "func_wall";
                        }
                    }
                    entity.Remove("solidity");
                    break;
                }
                case "env_fog_controller":
                {
                    entity["classname"] = "env_fog";
                    entity["rendercolor"] = entity["fogcolor"];
                    entity.Remove("fogcolor");
                    break;
                }
                case "prop_static":
                {
                    entity["classname"] = "item_generic";
                    break;
                }
                case "info_player_rebel":
                case "info_player_janus": // GoldenEye Source :3
                case "ctf_rebel_player_spawn":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "2";
                    _isCTF = true;
                    goto case "info_player_deathmatch";
                }
                case "info_player_combine":
                case "info_player_mi6":
                case "ctf_combine_player_spawn":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "1";
                    _isCTF = true;
                    goto case "info_player_deathmatch";
                }
                case "info_player_deathmatch":
                {
                    Vector3 origin = entity.Origin;
                    entity["origin"] = origin.X + " " + origin.Y + " " + (origin.Z + 40);
                    break;
                }
                case "ctf_combine_flag":
                {
                    entity.Remove("targetname");
                    entity.Remove("SpawnWithCaptureEnabled");
                    entity["skin"] = "1";
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["goal_no"] = "1";
                    entity["model"] = "models/ctf_flag.mdl";
                    entity["classname"] = "item_ctfflag";
                    Entity newFlagBase = new Entity("item_ctfbase");
                    newFlagBase["origin"] = entity["origin"];
                    newFlagBase["angles"] = entity["angles"];
                    newFlagBase["goal_max"] = "16 16 72";
                    newFlagBase["goal_min"] = "-16 -16 0";
                    newFlagBase["goal_no"] = "1";
                    newFlagBase["model"] = "models/ctf_flag_stand_mi6.mdl";
                    _entities.Add(newFlagBase);
                    _isCTF = true;
                    break;
                }
                case "ctf_rebel_flag":
                {
                    entity.Remove("targetname");
                    entity.Remove("SpawnWithCaptureEnabled");
                    entity["skin"] = "0";
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["goal_no"] = "2";
                    entity["model"] = "models/ctf_flag.mdl";
                    entity["classname"] = "item_ctfflag";
                    Entity newFlagBase = new Entity("item_ctfbase");
                    newFlagBase["origin"] = entity["origin"];
                    newFlagBase["angles"] = entity["angles"];
                    newFlagBase["goal_max"] = "16 16 72";
                    newFlagBase["goal_min"] = "-16 -16 0";
                    newFlagBase["goal_no"] = "2";
                    newFlagBase["model"] = "models/ctf_flag_stand_phoenix.mdl";
                    _entities.Add(newFlagBase);
                    _isCTF = true;
                    break;
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert an <see cref="Entity"/> from a Call of Duty BSP to one for Gearcraft.
        /// </summary>
        /// <param name="entity">The <see cref="Entity"/> to parse.</param>
        private void PostProcessCoDEntity(Entity entity)
        {
            if (entity.IsBrushBased)
            {
                Vector3 origin = entity.Origin;
                entity.Remove("origin");
                entity.Remove("model");
                if (entity.ValueIs("classname", "func_rotating"))
                {
                    // TODO: What entities require origin brushes in CoD?
                    if (origin == Vector3.Zero)
                    {
                        // If this brush uses the "origin" attribute
                        MAPBrush neworiginBrush = MAPBrushExtensions.CreateCube(new Vector3(-16, -16, -16), new Vector3(16, 16, 16), "special/origin");
                        entity.brushes.Add(neworiginBrush);
                    }
                }
                foreach (MAPBrush brush in entity.brushes)
                {
                    brush.Translate(origin);
                }
            }

            switch (entity["classname"].ToLower())
            {
                case "light":
                {
                    entity["_light"] = "255 255 255 " + entity["light"];
                    entity.Remove("light");
                    break;
                }
                case "mp_teamdeathmatch_spawn":
                case "mp_deathmatch_spawn":
                {
                    entity["classname"] = "info_player_deathmatch";
                    break;
                }
                case "mp_searchanddestroy_spawn_allied":
                {
                    entity["classname"] = "info_player_ctfspawn";
                    entity["team_no"] = "1";
                    entity.Remove("model");
                    _isCTF = true;
                    break;
                }
                case "mp_searchanddestroy_spawn_axis":
                {
                    entity["classname"] = "info_player_ctfspawn";
                    entity["team_no"] = "2";
                    entity.Remove("model");
                    _isCTF = true;
                    break;
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert an <see cref="Entity"/> from a Quake 2-based BSP to one for Gearcraft.
        /// </summary>
        /// <param name="entity">The <see cref="Entity"/> to parse.</param>
        private void PostProcessQuake2Entity(Entity entity)
        {
            if (!entity["angle"].Equals(""))
            {
                entity["angles"] = "0 " + entity["angle"] + " 0";
                entity.Remove("angle");
            }
            if (entity.IsBrushBased)
            {
                Vector3 origin = entity.Origin;
                entity.Remove("origin");
                entity.Remove("model");
                if (origin != Vector3.Zero)
                {
                    MAPBrush neworiginBrush = MAPBrushExtensions.CreateCube(new Vector3(-16, -16, -16), new Vector3(16, 16, 16), "special/origin");
                    entity.brushes.Add(neworiginBrush);
                }
                foreach (MAPBrush brush in entity.brushes)
                {
                    brush.Translate(origin);
                }
            }

            switch (entity["classname"].ToLower())
            {
                case "func_wall":
                {
                    if (entity.SpawnflagsSet(2) || entity.SpawnflagsSet(4))
                    {
                        entity["classname"] = "func_wall_toggle";
                    }
                    break;
                }
                case "item_flag_team2":
                case "ctf_flag_hardcorps":
                {
                    // Blue flag
                    entity["classname"] = "item_ctfflag";
                    entity["skin"] = "1"; // 0 for PHX, 1 for MI6
                    entity["goal_no"] = "1"; // 2 for PHX, 1 for MI6
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["model"] = "models/ctf_flag.mdl";
                    Entity flagBase = new Entity("item_ctfbase");
                    flagBase["origin"] = entity["origin"];
                    flagBase["angles"] = entity["angles"];
                    flagBase["goal_no"] = "1";
                    flagBase["model"] = "models/ctf_flag_stand_mi6.mdl";
                    flagBase["goal_max"] = "16 16 72";
                    flagBase["goal_min"] = "-16 -16 0";
                    _entities.Add(flagBase);
                    _isCTF = true;
                    break;
                }
                case "item_flag_team1":
                case "ctf_flag_sintek":
                {
                    // Red flag
                    entity["classname"] = "item_ctfflag";
                    entity["skin"] = "0"; // 0 for PHX, 1 for MI6
                    entity["goal_no"] = "2"; // 2 for PHX, 1 for MI6
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["model"] = "models/ctf_flag.mdl";
                    Entity flagBase = new Entity("item_ctfbase");
                    flagBase["origin"] = entity["origin"];
                    flagBase["angles"] = entity["angles"];
                    flagBase["goal_no"] = "2";
                    flagBase["model"] = "models/ctf_flag_stand_phoenix.mdl";
                    flagBase["goal_max"] = "16 16 72";
                    flagBase["goal_min"] = "-16 -16 0";
                    _entities.Add(flagBase);
                    _isCTF = true;
                    break;
                }
                case "info_player_team1":
                case "info_player_sintek":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "2";
                    Vector3 origin = entity.Origin;
                    entity["origin"] = origin.X + " " + origin.Y + " " + (origin.Z + 18);
                    _isCTF = true;
                    break;
                }
                case "info_player_team2":
                case "info_player_hardcorps":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "1";
                    Vector3 origin = entity.Origin;
                    entity["origin"] = origin.X + " " + origin.Y + " " + (origin.Z + 18);
                    _isCTF = true;
                    break;
                }
                case "info_player_start":
                case "info_player_coop":
                case "info_player_deathmatch":
                {
                    Vector3 origin = entity.Origin;
                    entity["origin"] = origin.X + " " + origin.Y + " " + (origin.Z + 18);
                    break;
                }
                case "light":
                {
                    Vector4 color;
                    if (entity.ContainsKey("_color"))
                    {
                        color = entity.GetVector("_color");
                    }
                    else
                    {
                        color = Vector4.One;
                    }
                    color *= 255;
                    float intensity = entity.GetFloat("light", 1);
                    entity.Remove("_color");
                    entity.Remove("light");
                    entity["_light"] = color.X + " " + color.Y + " " + color.Z + " " + intensity;
                    break;
                }
                case "misc_teleporter":
                {
                    Vector3 origin = entity.Origin;
                    Vector3 mins = new Vector3(origin.X - 24, origin.Y - 24, origin.Z - 24);
                    Vector3 maxs = new Vector3(origin.X + 24, origin.Y + 24, origin.Z + 48);
                    entity.brushes.Add(MAPBrushExtensions.CreateCube(mins, maxs, "special/trigger"));
                    entity.Remove("origin");
                    entity["classname"] = "trigger_teleport";
                    break;
                }
                case "misc_teleporter_dest":
                {
                    entity["classname"] = "info_teleport_destination";
                    break;
                }
                case "target_speaker":
                {
                    entity.ClassName = "ambient_generic";
                    entity.RenameKey("noise", "message");

                    float newVolume = 10;
                    uint newSpawnflags = 0;

                    // "Looped Off"
                    if (entity.SpawnflagsSet(2))
                    {
                        // "Is NOT Looped"
                        newSpawnflags |= 32;
                    }

                    if (entity.ContainsKey("attenuation"))
                    {
                        // Clear bits 0, 1, 2 and 3
                        newSpawnflags &= ~(uint)15;
                        switch (entity.GetInt("attenuation"))
                        {
                            case 1:
                            {
                                // "Small Radius"
                                newSpawnflags |= 2;
                                break;
                            }
                            case -1:
                            case 2:
                            {
                                // "Medium Radius"
                                newSpawnflags |= 4;
                                break;
                            }
                            case 3:
                            {
                                // "Large Radius"
                                newSpawnflags |= 8;
                                break;
                            }
                        }
                        entity.Remove("attenuation");
                    }
                    else
                    {
                        // "Looped On"
                        if (entity.SpawnflagsSet(1))
                        {
                            // "Large Radius"
                            newSpawnflags |= 8;
                        }
                        else
                        {
                            // "Small Radius"
                            newSpawnflags |= 2;
                        }
                    }

                    if (entity.ContainsKey("volume"))
                    {
                        float volume = entity.GetFloat("volume", 1);
                        newVolume = volume * 10f;
                        entity.Remove("volume");
                    }

                    entity["health"] = newVolume.ToString();
                    entity.Spawnflags = newSpawnflags;

                    entity["pitchstart"] = "100";
                    entity["pitch"] = "100";
                    break;
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert an <see cref="Entity"/> from a Quake 3-based BSP to one for Gearcraft.
        /// </summary>
        /// <param name="entity">The <see cref="Entity"/> to parse.</param>
        private void PostProcessQuake3Entity(Entity entity)
        {
            if (entity.IsBrushBased)
            {
                Vector3 origin = entity.Origin;
                entity.Remove("origin");
                entity.Remove("model");
                if (entity.ValueIs("classname", "func_rotating") || entity.ValueIs("classname", "func_rotatingdoor"))
                {
                    // TODO: What entities require origin brushes in Quake 3?
                    if (origin != Vector3.Zero)
                    {
                        MAPBrush neworiginBrush = MAPBrushExtensions.CreateCube(new Vector3(-16, -16, -16), new Vector3(16, 16, 16), "special/origin");
                        entity.brushes.Add(neworiginBrush);
                    }
                }
                foreach (MAPBrush brush in entity.brushes)
                {
                    brush.Translate(origin);
                }
            }

            switch (entity["classname"].ToLower())
            {
                case "worldspawn":
                {
                    if (!entity["suncolor"].Equals(""))
                    {
                        Entity light_environment = new Entity("light_environment");
                        light_environment["_light"] = entity["suncolor"];
                        light_environment["angles"] = entity["sundirection"];
                        light_environment["_fade"] = entity["sundiffuse"];
                        entity.Remove("suncolor");
                        entity.Remove("sundirection");
                        entity.Remove("sundiffuse");
                        entity.Remove("sundiffusecolor");
                        _entities.Add(light_environment);
                    }
                    break;
                }
                case "team_ctf_blueflag":
                {
                    // Blue flag
                    entity["classname"] = "item_ctfflag";
                    entity["skin"] = "1"; // 0 for PHX, 1 for MI6
                    entity["goal_no"] = "1"; // 2 for PHX, 1 for MI6
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["model"] = "models/ctf_flag.mdl";
                    Entity flagBase = new Entity("item_ctfbase");
                    flagBase["origin"] = entity["origin"];
                    flagBase["angles"] = entity["angles"];
                    flagBase["angle"] = entity["angle"];
                    flagBase["goal_no"] = "1";
                    flagBase["model"] = "models/ctf_flag_stand_mi6.mdl";
                    flagBase["goal_max"] = "16 16 72";
                    flagBase["goal_min"] = "-16 -16 0";
                    _entities.Add(flagBase);
                    _isCTF = true;
                    break;
                }
                case "team_ctf_redflag":
                {
                    // Red flag
                    entity["classname"] = "item_ctfflag";
                    entity["skin"] = "0"; // 0 for PHX, 1 for MI6
                    entity["goal_no"] = "2"; // 2 for PHX, 1 for MI6
                    entity["goal_max"] = "16 16 72";
                    entity["goal_min"] = "-16 -16 0";
                    entity["model"] = "models/ctf_flag.mdl";
                    Entity flagBase = new Entity("item_ctfbase");
                    flagBase["origin"] = entity["origin"];
                    flagBase["angles"] = entity["angles"];
                    flagBase["angle"] = entity["angle"];
                    flagBase["goal_no"] = "2";
                    flagBase["model"] = "models/ctf_flag_stand_phoenix.mdl";
                    flagBase["goal_max"] = "16 16 72";
                    flagBase["goal_min"] = "-16 -16 0";
                    _entities.Add(flagBase);
                    _isCTF = true;
                    break;
                }
                case "team_ctf_redspawn":
                case "info_player_axis":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "2";
                    _isCTF = true;
                    goto case "info_player_start";
                }
                case "team_ctf_bluespawn":
                case "info_player_allied":
                {
                    entity["classname"] = "info_ctfspawn";
                    entity["team_no"] = "1";
                    _isCTF = true;
                    goto case "info_player_start";
                }
                case "info_player_start":
                case "info_player_coop":
                case "info_player_deathmatch":
                {
                    Vector3 origin = entity.Origin;
                    entity["origin"] = origin.X + " " + origin.Y + " " + (origin.Z + 24);
                    break;
                }
                case "light":
                {
                    Vector4 color;
                    if (entity.ContainsKey("_color"))
                    {
                        color = entity.GetVector("_color");
                    }
                    else
                    {
                        color = Vector4.One;
                    }
                    color *= 255;
                    float intensity = entity.GetFloat("light", 1);
                    entity.Remove("_color");
                    entity.Remove("light");
                    entity["_light"] = color.X + " " + color.Y + " " + color.Z + " " + intensity;
                    break;
                }
                case "func_rotatingdoor":
                {
                    entity["classname"] = "func_door_rotating";
                    break;
                }
                case "info_pathnode":
                {
                    entity["classname"] = "info_node";
                    break;
                }
                case "trigger_ladder":
                {
                    entity["classname"] = "func_ladder";
                    break;
                }
                case "trigger_use":
                {
                    entity["classname"] = "func_button";
                    entity["spawnflags"] = "1";
                    entity["wait"] = "1";
                    break;
                }
            }
        }

        /// <summary>
        /// Every <see cref="MAPBrushSide"/> contained in <paramref name="brushes"/> will have its texture examined,
        /// and, if necessary, replaced with the equivalent for Gearcraft.
        /// </summary>
        /// <param name="brushes">The collection of <see cref="MAPBrush"/> objects to have textures parsed.</param>
        /// <param name="version">The <see cref="MapType"/> of the BSP this entity came from.</param>
        private void PostProcessTextures(IEnumerable<MAPBrush> brushes)
        {
            foreach (MAPBrush brush in brushes)
            {
                foreach (MAPBrushSide brushSide in brush.sides)
                {
                    brushSide.textureInfo.Validate(brushSide.plane);
                    PostProcessSpecialTexture(brushSide);

                    if (_version.IsSubtypeOf(MapType.Quake2))
                    {
                        PostProcessQuake2Texture(brushSide);
                    }
                    else if (_version.IsSubtypeOf(MapType.Quake3))
                    {
                        PostProcessQuake3Texture(brushSide);
                    }
                    else if (_version.IsSubtypeOf(MapType.Source))
                    {
                        PostProcessSourceTexture(brushSide);
                    }
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Hammer, if necessary.
        /// </summary>
        /// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
        private void PostProcessQuake2Texture(MAPBrushSide brushSide)
        {
            if (brushSide.texture.EndsWith("/clip", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/clip";
            }
            else if (brushSide.texture.EndsWith("/hint", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/hint";
            }
            else if (brushSide.texture.EndsWith("/trigger", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/trigger";
            }
            else if (brushSide.texture.EndsWith("/skip", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/skip";
            }
            else if (brushSide.texture.EndsWith("/sky1", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/sky";
            }
            else if (brushSide.texture.Equals("*** unsused_texinfo ***", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/nodraw";
            }
        }

        /// <summary>
        /// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by GTKRadiant, if necessary.
        /// These textures are produced by the decompiler algorithm itself.
        /// </summary>
        /// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
        private void PostProcessSpecialTexture(MAPBrushSide brushSide)
        {
            switch (brushSide.texture.ToLower())
            {
                case "**nulltexture**":
                {
                    brushSide.texture = "special/null";
                    break;
                }
                case "**skiptexture**":
                {
                    brushSide.texture = "special/skip";
                    break;
                }
                case "**skytexture**":
                {
                    brushSide.texture = "special/sky";
                    break;
                }
                case "**hinttexture**":
                {
                    brushSide.texture = "special/hint";
                    break;
                }
                case "**cliptexture**":
                {
                    brushSide.texture = "special/clip";
                    break;
                }
                case "**nodrawtexture**":
                {
                    brushSide.texture = "special/nodraw";
                    break;
                }
            }
        }

        /// <summary>
        /// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Gearcraft, if necessary.
        /// </summary>
        /// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
        private void PostProcessQuake3Texture(MAPBrushSide brushSide)
        {
            if (brushSide.texture.StartsWith("textures/", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = brushSide.texture.Substring(9);
            }
            switch (brushSide.texture.ToLower())
            {
                case "common/physics_clip":
                case "common/metalclip":
                case "common/grassclip":
                case "common/paperclip":
                case "common/woodclip":
                case "common/glassclip":
                case "common/clipfoliage":
                case "common/foliageclip":
                case "common/carpetclip":
                case "common/dirtclip":
                case "system/clip":
                case "system/physics_clip":
                case "common/clip":
                {
                    brushSide.texture = "special/clip";
                    break;
                }
                case "common/nodrawnonsolid":
                case "system/trigger":
                case "common/trigger":
                {
                    brushSide.texture = "special/trigger";
                    break;
                }
                case "common/nodraw":
                case "common/caulkshadow":
                case "common/caulk":
                case "system/caulk":
                case "noshader":
                {
                    brushSide.texture = "special/nodraw";
                    break;
                }
                case "common/do_not_enter":
                case "common/donotenter":
                case "common/monsterclip":
                {
                    brushSide.texture = "special/npcclip";
                    break;
                }
                case "common/caulksky":
                case "common/skyportal":
                {
                    brushSide.texture = "special/sky";
                    break;
                }
                case "common/hint":
                {
                    brushSide.texture = "special/hint";
                    break;
                }
                case "common/waterskip":
                {
                    brushSide.texture = "liquids/!water";
                    break;
                }
                case "system/do_not_enter":
                case "common/playerclip":
                {
                    brushSide.texture = "special/playerclip";
                    break;
                }
            }
            if (brushSide.texture.StartsWith("sky/", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = "special/sky";
            }
        }

        /// <summary>
        /// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Gearcraft, if necessary.
        /// </summary>
        /// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
        private void PostProcessSourceTexture(MAPBrushSide brushSide)
        {
            if (brushSide.texture.StartsWith("maps/", StringComparison.InvariantCultureIgnoreCase))
            {
                brushSide.texture = brushSide.texture.Substring(5);
                for (int i = 0; i < brushSide.texture.Length; ++i)
                {
                    if (brushSide.texture[i] == '/')
                    {
                        brushSide.texture = brushSide.texture.Substring(i + 1);
                        break;
                    }
                }
            }

            switch (brushSide.texture.ToLower())
            {
                case "tools/toolshint":
                {
                    brushSide.texture = "special/hint";
                    break;
                }
                case "tools/toolsskip":
                {
                    brushSide.texture = "special/skip";
                    break;
                }
                case "tools/toolsinvisible":
                case "tools/toolsclip":
                {
                    brushSide.texture = "special/clip";
                    break;
                }
                case "tools/toolstrigger":
                case "tools/toolsfog":
                {
                    brushSide.texture = "special/trigger";
                    break;
                }
                case "tools/toolsskybox":
                {
                    brushSide.texture = "special/sky";
                    break;
                }
                case "tools/toolsnodraw":
                {
                    brushSide.texture = "special/nodraw";
                    break;
                }
                case "tools/toolsplayerclip":
                {
                    brushSide.texture = "special/playerclip";
                    break;
                }
                case "tools/toolsnpcclip":
                {
                    brushSide.texture = "special/enemyclip";
                    break;
                }
                case "tools/toolsblack":
                {
                    brushSide.texture = "special/black";
                    break;
                }
            }

            // Parse cubemap textures
            // I'm sure this could be done more concisely with regex, but I suck at regex.
            int numUnderscores = 0;
            bool validnumber = false;
            for (int i = brushSide.texture.Length - 1; i > 0; --i)
            {
                if (brushSide.texture[i] <= '9' && brushSide.texture[i] >= '0')
                {
                    // Current is a number, this may be a cubemap reference
                    validnumber = true;
                }
                else
                {
                    if (brushSide.texture[i] == '-')
                    {
                        // Current is a minus sign (-).
                        if (!validnumber)
                        {
                            break; // Make sure there's a number to add the minus sign to. If not, kill the loop.
                        }
                    }
                    else
                    {
                        if (brushSide.texture[i] == '_')
                        {
                            // Current is an underscore (_)
                            if (validnumber)
                            {
                                // Make sure there is a number in the current string
                                ++numUnderscores; // before moving on to the next one.
                                if (numUnderscores == 3)
                                {
                                    // If we've got all our numbers
                                    brushSide.texture = brushSide.texture.Substring(0, i); // Cut the texture string
                                    break; // Kill the loop, we're done
                                }
                                validnumber = false;
                            }
                            else
                            {
                                // No number after the underscore
                                break;
                            }
                        }
                        else
                        {
                            // Not an acceptable character
                            break;
                        }
                    }
                }
            }

        }

    }
}
