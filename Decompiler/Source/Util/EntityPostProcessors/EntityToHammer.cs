using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Class containing methods for parsing entities from various BSP formats into those for Hammer.
	/// </summary>
	public class EntityToHammer {

		private Job _master;

		private Entities _entities;
		private MapType _version;

		private int _mmStackLength = 0;
		private List<string> _numeralizedTargetnames = new List<string>();
		private List<int> _numTargets = new List<int>();

		/// <summary>
		/// Creates a new instance of an <see cref="EntityToHammer"/> object which will operate on the passed <see cref="Entities"/>.
		/// </summary>
		/// <param name="entities">The <see cref="Entities"/> to postprocess.</param>
		/// <param name="version">The <see cref="MapType"/> of the BSP the entities are from.</param>
		/// <param name="master">The parent <see cref="Job"/> object for this instance.</param>
		public EntityToHammer(Entities entities, MapType version, Job master) {
			_entities = entities;
			_version = version;
			_master = master;
		}

		/// <summary>
		/// Processes every <see cref="Entity"/> in an <see cref="Entities"/> object to be used in a Hammer map.
		/// </summary>
		public void PostProcessEntities() {
			// There should really only be one of these. But someone might have screwed with the map...
			List<Entity> worldspawns = _entities.FindAll(entity => { return entity.ClassName.Equals("worldspawn", StringComparison.InvariantCultureIgnoreCase); });

			if (_version.IsSubtypeOf(MapType.Source)) {
				bool hasWater = false;
				// Make sure all water brushes currently in the world get converted to Source.
				foreach (Entity worldspawn in worldspawns) {
					for (int i = 0; i < worldspawn.brushes.Count; ++i) {
						MAPBrush brush = worldspawn.brushes[i];
						if (brush.isWater) {
							hasWater = true;
							ConvertToWater(brush);
						} else if (brush.isManVis) {
							// Remove "manual vis" brushes
							worldspawn.brushes.RemoveAt(i--);
						} else if (brush.mohTerrain != null) {
							// Convert MoHAA terrain to a displacement
							MAPBrush newBrush = ConvertToDisplacement(brush.mohTerrain);
							worldspawn.brushes.RemoveAt(i--);
							if (newBrush != null) {
								worldspawn.brushes.Add(newBrush);
							}
						} else if (brush.ef2Terrain != null) {
							// Convert EF2 terrain to a displacement
							MAPBrush newBrush = ConvertToDisplacement(brush.ef2Terrain);
							worldspawn.brushes.RemoveAt(i--);
							if (newBrush != null) {
								worldspawn.brushes.Add(newBrush);
							}
						}
					}
				}
				// Make sure all func_water entities get converted to Source.
				List<Entity> waters = _entities.FindAll(entity => { return entity.ClassName.Equals("func_water", StringComparison.InvariantCultureIgnoreCase); });
				if (waters.Any()) {
					hasWater = true;
					// Parse water entities into just water brushes
					foreach (Entity water in waters) {
						ParseWaterIntoWorld(worldspawns[0], water);
						_entities.Remove(water);
					}
				}

				if (hasWater && !_entities.Any(entity => { return entity.ClassName.Equals("water_lod_control", StringComparison.InvariantCultureIgnoreCase); })) {
					Entity pointEntity = _entities.Find(entity => { return !entity.IsBrushBased; });
					Vector3 origin = Vector3.Zero;
					if (pointEntity != null) {
						origin = pointEntity.Origin;
					}
					Entity lodControl = new Entity("water_lod_control");
					lodControl["cheapwaterenddistance"] = "2000";
					lodControl["cheapwaterstartdistance"] = "1000";
					lodControl.Origin = origin;
				}
			}

			foreach (Entity worldspawn in worldspawns) {
				for (int i = 0; i < worldspawn.brushes.Count; ++i) {
					MAPBrush brush = worldspawn.brushes[i];
					bool isDisplacement = false;
					foreach (MAPBrushSide side in brush.sides) {
						if (side.displacement != null) {
							isDisplacement = true;
							break;
						}
					}
					if (brush.isDetail && !isDisplacement) {
						Entity newEntity = new Entity("func_detail");
						newEntity.brushes.Add(brush);
						_entities.Add(newEntity);
						worldspawn.brushes.RemoveAt(i);
						--i;
					}
				}
			}

			// We might modify the collection as we iterate over it. Can't use foreach.
			for (int i = 0; i < _entities.Count; ++i) {
				if (!_master.settings.noEntCorrection) {
					PostProcessEntity(_entities[i]);
				}
				if (!_master.settings.noTexCorrection) {
					PostProcessTextures(_entities[i].brushes);
				}
			}

			if (!_master.settings.noEntCorrection) {
				if (_version.IsSubtypeOf(MapType.Source)) {
					for (int i = 0; i < _entities.Count; ++i) {
						ParseEntityIO(_entities[i]);
					}
				}
			}
		}

		/// <summary>
		/// Goes through each <see cref="MAPBrush"/> in <paramref name="water"/>, converts it to a water brush for Source,
		/// and adds the <see cref="MAPBrush"/> to <paramref name="world"/>.
		/// </summary>
		/// <param name="world">The world <see cref="Entity"/>.</param>
		/// <param name="water">A water <see cref="Entity"/>.</param>
		private void ParseWaterIntoWorld(Entity world, Entity water) {
			foreach (MAPBrush brush in water.brushes) {
				ConvertToWater(brush);
				world.brushes.Add(brush);
			}
		}

		/// <summary>
		/// For <paramref name="brush"/>, sets the top <see cref="MAPBrushSide"/>'s texture
		/// to a water texture and sets all others to nodraw.
		/// </summary>
		/// <param name="brush">The <see cref="MAPBrush"/> to make into a water brush.</param>
		private void ConvertToWater(MAPBrush brush) {
			foreach (MAPBrushSide side in brush.sides) {
				if (side.plane.Normal == Vector3.UnitZ) {
					side.texture = "dev/dev_water2";
				} else {
					side.texture = "TOOLS/TOOLSNODRAW";
				}
			}
		}

		/// <summary>
		/// Converts the passed <see cref="MAPTerrainMoHAA"/> to a <see cref="MAPDisplacement"/>
		/// contained within the returned <see cref="MAPBrush"/>.
		/// </summary>
		/// <param name="mohTerrain">The <see cref="MAPTerrainMoHAA"/> to convert.</param>
		/// <returns><see cref="MAPBrush"/> containing a <see cref="MAPDisplacement"/> imitating the heightmap of <paramref name="terrain"/>.</returns>
		private MAPBrush ConvertToDisplacement(MAPTerrainMoHAA terrain) {
			if (terrain.size == new Vector2(9, 9)) {
				MAPTerrainMoHAA.Partition partition = terrain.partitions[0];
				MAPBrush newBrush = CreateBrushForTerrain(terrain.origin, 512, terrain.flags > 0, partition.shader, partition.rotation,
				                                          new float[] { partition.textureScale[0], partition.textureScale[1] },
				                                          new float[] { partition.textureShift[0], partition.textureShift[1] });
				MAPBrushSide newSide = newBrush.sides[0];
				MAPDisplacement newDisplacement = new MAPDisplacement() {
					power = 3,
					start = terrain.origin,
					normals = new Vector3[9, 9],
					distances = new float[9, 9],
					alphas = new float[9, 9],
				};
				for (int y = 0; y < terrain.size.Y; ++y) {
					for (int x = 0; x < terrain.size.X; ++x) {
						newDisplacement.normals[y, x] = Vector3.UnitZ;
						if (terrain.flags > 0) {
							newDisplacement.distances[y, x] = terrain.vertices[(x * (int)terrain.size.Y) + y].height;
						} else {
							newDisplacement.distances[y, x] = terrain.vertices[(y * (int)terrain.size.Y) + x].height;
						}
					}
				}
				newSide.displacement = newDisplacement;
				return newBrush;
			}
			return null;
		}

		/// <summary>
		/// Converts the passed <see cref="MAPTerrainEF2"/> to a <see cref="MAPDisplacement"/>
		/// contained within the returned <see cref="MAPBrush"/>.
		/// </summary>
		/// <param name="mohTerrain">The <see cref="MAPTerrainEF2"/> to convert.</param>
		/// <returns><see cref="MAPBrush"/> containing a <see cref="MAPDisplacement"/> imitating the heightmap of <paramref name="terrain"/>.</returns>
		private MAPBrush ConvertToDisplacement(MAPTerrainEF2 terrain) {
			if (terrain.side == 9) {
				MAPBrush newBrush = CreateBrushForTerrain(terrain.start, terrain.sideLength, false, terrain.texture, terrain.texRot,
				                                          new float[] { terrain.texScaleX, terrain.texScaleY },
				                                          new float[] { terrain.textureShiftS, terrain.textureShiftT });
				MAPBrushSide newSide = newBrush.sides[0];
				MAPDisplacement newDisplacement = new MAPDisplacement() {
					power = 3,
					start = terrain.start,
					normals = new Vector3[9, 9],
					distances = new float[9, 9],
					alphas = new float[9, 9],
				};
				for (int y = 0; y < terrain.side; ++y) {
					for (int x = 0; x < terrain.side; ++x) {
						newDisplacement.normals[y, x] = Vector3.UnitZ;
						newDisplacement.distances[y, x] = terrain.heightMap[y, x];
					}
				}
				newSide.displacement = newDisplacement;
				return newBrush;
			}
			return null;
		}

		/// <summary>
		/// Creates a square <see cref="MAPBrush"/> with side length <see cref="side"/> using <paramref name="origin"/> as a starting point.
		/// </summary>
		/// <param name="origin">The minimum X and Y extents of the new <see cref="MAPBrush"/>, Z will be the height of the face with <paramref name="texture"/> on it.</param>
		/// <param name="side">Length of the side of the brush.</param>
		/// <param name="inverted">Should the first side be on the bottom of the returned <see cref="MAPBrush"/> (If <c>false</c>, the first side will be on top)?</param>
		/// <param name="texture">The texture to use for the first side.</param>
		/// <param name="rotation">The rotation of the texture on the first side.</param>
		/// <param name="textureScale">The scale of the texture on the first side.</param>
		/// <param name="textureShift">The position of the texture on the first side.</param>
		/// <returns>A <see cref="MAPBrush"/> with <paramref name="texture"/> applied to the first face with the passed attributes.</returns>
		private MAPBrush CreateBrushForTerrain(Vector3 origin, float side, bool inverted, string texture, float rotation, float[] textureScale, float[] textureShift) {
			Vector3[] froms = new Vector3[] {
				origin,
				new Vector3(origin.X, origin.Y + side, origin.Z),
				new Vector3(origin.X + side, origin.Y + side, origin.Z),
				new Vector3(origin.X + side, origin.Y, origin.Z),
			};
			Vector3[] tos = new Vector3[] {
				new Vector3(origin.X, origin.Y + side, origin.Z),
				new Vector3(origin.X + side, origin.Y + side, origin.Z),
				new Vector3(origin.X + side, origin.Y, origin.Z),
				origin,
			};
			if (inverted) {
				Vector3 temp = froms[1];
				froms[1] = froms[3];
				froms[3] = temp;
				temp = tos[0];
				tos[0] = tos[2];
				tos[2] = temp;
			}
			Vector3[] axes = TextureInfo.TextureAxisFromPlane(Plane.CreateFromVertices(froms[0], froms[2], froms[1]));
			TextureInfo newTextureInfo = new TextureInfo(axes[0], axes[1],
			                                             new Vector2(textureShift[0], textureShift[1]),
			                                             new Vector2(textureScale[0], textureScale[1]),
			                                             0, 0, rotation);
			return MAPBrushExtensions.CreateBrushFromWind(froms, tos, texture, "tools/toolsnodraw", newTextureInfo, 32);
		}

		/// <summary>
		/// Sends <paramref name="entity"/> to be postprocessed into the appropriate method based on version.
		/// </summary>
		/// <param name="entity"><see cref="Entity"/> to postprocess.</param>
		private void PostProcessEntity(Entity entity) {
			if (entity.IsBrushBased) {
				Vector3 origin = entity.Origin;
				entity.Remove("model");
				foreach (MAPBrush brush in entity.brushes) {
					brush.Translate(origin);
				}
			}

			if (_version.IsSubtypeOf(MapType.Quake2)) {
				PostProcessQuake2Entity(entity);
			} else if (_version == MapType.Nightfire) {
				PostProcessNightfireEntity(entity);
			} else if (_version.IsSubtypeOf(MapType.Source)) {
				PostProcessSourceEntity(entity);
			} else if (_version.IsSubtypeOf(MapType.Quake3)) {
				PostProcessQuake3Entity(entity);
			}
		}

		/// <summary>
		/// Postprocesser to convert an <see cref="Entity"/> from a Nightfire BSP to one for Hammer.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to parse.</param>
		private void PostProcessNightfireEntity(Entity entity) {
			if (entity.Angles.X != 0) {
				entity.Angles = new Vector3(-entity.Angles.X, entity.Angles.Y, entity.Angles.Z);
			}
			if (!entity["body"].Equals("")) {
				entity.RenameKey("body", "SetBodyGroup");
			}
			if (entity.GetVector("rendercolor") == Vector4.Zero) {
				entity["rendercolor"] = "255 255 255";
			}
			if (entity.Angles == new Vector3(0, -1, 0)) {
				entity.Angles = new Vector3(-90, 0, 0);
			}
			string modelName = entity["model"];
			if (modelName.Length >= 4 && modelName.Substring(modelName.Length - 4).Equals(".spz", StringComparison.InvariantCultureIgnoreCase)) {
				entity["model"] = modelName.Substring(0, modelName.Length - 4) + ".spr";
			}

			switch (entity.ClassName.ToLower()) {
				case "light_spot": {
					entity["pitch"] = (entity.Angles.X + entity.GetFloat("pitch", 0)).ToString();
					float cone = entity.GetFloat("_cone", 0);
					if (cone > 90) { cone = 90; }
					if (cone < 0) { cone = 0; }
					entity["_cone"] = cone.ToString();
					float cone2 = entity.GetFloat("_cone2", 0);
					if (cone2 > 90) { cone2 = 90; }
					if (cone2 < 0) { cone2 = 0; }
					entity["_cone2"] = cone2.ToString();
					entity.RenameKey("_cone", "_inner_cone");
					entity.RenameKey("_cone2", "_cone");
					break;
				}
				case "func_wall": {
					entity["classname"] = "func_brush";
					entity["solidity"] = "2";
					entity["disableshadows"] = "1";
					entity.Remove("angles");
					entity.Remove("rendermode");
					break;
				}
				case "func_wall_toggle": {
					entity["classname"] = "func_brush";
					entity["solidity"] = "0";
					entity["disableshadows"] = "1";
					entity.Remove("angles");
					if (entity.SpawnflagsSet(1)) {
						entity["StartDisabled"] = "1";
						entity.ClearSpawnflags(1);
					} else {
						entity["StartDisabled"] = "0";
					}
					break;
				}
				case "func_illusionary": {
					entity["classname"] = "func_brush";
					entity["solidity"] = "1";
					entity["disableshadows"] = "1";
					entity.Remove("angles");
					break;
				}
				case "item_generic": {
					entity["classname"] = "prop_dynamic";
					entity["solid"] = "0";
					entity.Remove("effects");
					entity.Remove("fixedlight");
					break;
				}
				case "env_glow": {
					entity["classname"] = "env_sprite";
					break;
				}
				case "info_teleport_destination": {
					entity["classname"] = "info_target";
					break;
				}
				case "info_ctfspawn": {
					if (entity["team_no"].Equals("1")) {
						entity["classname"] = "ctf_combine_player_spawn";
						entity.Remove("team_no");
					} else if (entity["team_no"].Equals("2")) {
						entity["classname"] = "ctf_rebel_player_spawn";
						entity.Remove("team_no");
					}
					goto case "info_player_start";
				}
				case "info_player_deathmatch":
				case "info_player_start": {
					Vector3 origin = entity.Origin;
					entity.Origin = new Vector3(origin.X, origin.Y, (origin.Z - 40));
					break;
				}
				case "item_ctfflag": {
					entity.Remove("skin");
					entity.Remove("goal_min");
					entity.Remove("goal_max");
					entity.Remove("model");
					entity["SpawnWithCaptureEnabled"] = "1";
					if (entity["goal_no"].Equals("1")) {
						entity["classname"] = "ctf_combine_flag";
						entity["targetname"] = "combine_flag";
						entity.Remove("goal_no");
					} else if (entity["goal_no"].Equals("2")) {
						entity["classname"] = "ctf_rebel_flag";
						entity["targetname"] = "rebel_flag";
						entity.Remove("goal_no");
					}
					break;
				}
				case "func_ladder": {
					foreach (MAPBrush brush in entity.brushes) {
						foreach (MAPBrushSide side in brush.sides) {
							side.texture = "TOOLS/TOOLSINVISIBLELADDER";
						}
					}
					break;
				}
				case "func_door": {
					entity["movedir"] = entity["angles"];
					entity["noise1"] = entity["movement_noise"];
					entity.Remove("movement_noise");
					entity.Remove("angles");
					if (entity.SpawnflagsSet(1)) {
						entity["spawnpos"] = "1";
						entity.ClearSpawnflags(1);
					}
					entity["renderamt"] = "255";
					break;
				}
				case "func_button": {
					entity["movedir"] = entity["angles"];
					goto case "func_rot_button";
				}
				case "func_rot_button": {
					entity.Remove("angles");
					foreach (MAPBrush brush in entity.brushes) {
						foreach (MAPBrushSide side in brush.sides) {
							// If we want this to be an invisible, non-colliding button that's "+use"-able
							if (side.texture.Equals("special/TRIGGER", StringComparison.InvariantCultureIgnoreCase)) {
								side.texture = "TOOLS/TOOLSHINT"; // Hint is the only thing that still works that doesn't collide with the player
							}
						}
					}
					if (!entity.SpawnflagsSet(256)) {
						// Nightfire's "touch activates" flag, same as source!
						if (entity.GetFloat("health", 0) != 0) {
							entity.SetSpawnflags(512);
						} else {
							entity.SetSpawnflags(1024);
						}
					}
					break;
				}
				case "trigger_hurt": {
					if (entity.SpawnflagsSet(2)) {
						entity["StartDisabled"] = "1";
					}
					if (!entity.SpawnflagsSet(8)) {
						entity["spawnflags"] = "1";
					} else {
						entity["spawnflags"] = "0";
					}
					entity.RenameKey("dmg", "damage");
					break;
				}
				case "trigger_auto": {
					entity["classname"] = "logic_auto";
					break;
				}
				case "trigger_once":
				case "trigger_multiple": {
					if (entity.SpawnflagsSet(8) || entity.SpawnflagsSet(1)) {
						entity.ClearSpawnflags(1);
						entity.ClearSpawnflags(8);
						entity.SetSpawnflags(2);
					}
					if (entity.SpawnflagsSet(2)) {
						entity.ClearSpawnflags(1);
					} else {
						entity.SetSpawnflags(1);
					}
					break;
				}
				case "func_door_rotating": {
					if (entity.SpawnflagsSet(1)) {
						entity["spawnpos"] = "1";
						entity.ClearSpawnflags(1);
					}
					entity["noise1"] = entity["movement_noise"];
					entity.Remove("movement_noise");
					break;
				}
				case "trigger_push": {
					entity["pushdir"] = entity["angles"];
					entity.Remove("angles");
					break;
				}
				case "light_environment": {
					Entity newShadowControl = new Entity("shadow_control");
					Entity newEnvSun = new Entity("env_sun");
					newShadowControl["angles"] = entity["angles"];
					newEnvSun["angles"] = entity["angles"];
					newShadowControl["origin"] = entity["origin"];
					newEnvSun["origin"] = entity["origin"];
					newShadowControl["color"] = "128 128 128";
					_entities.Add(newShadowControl);
					_entities.Add(newEnvSun);
					break;
				}
				case "func_tracktrain": {
					entity.RenameKey("movesnd", "MoveSound");
					entity.RenameKey("stopsnd", "StopSound");
					break;
				}
				case "path_track": {
					if (entity.SpawnflagsSet(1)) {
						entity.Remove("targetname");
					}
					break;
				}
				case "trigger_relay": {
					entity["classname"] = "logic_relay";
					break;
				}
				case "trigger_counter": {
					entity["classname"] = "math_counter";
					entity["max"] = entity["count"];
					entity["min"] = "0";
					entity["startvalue"] = "0";
					entity.Remove("count");
					break;
				}
				case "worldspawn": {
					entity.Remove("mapversion");
					break;
				}
			}
		}

		/// <summary>
		/// Postprocesser to convert an <see cref="Entity"/> from a Source engine BSP to one for Hammer.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to parse.</param>
		private void PostProcessSourceEntity(Entity entity) {
			entity.Remove("hammerid");

		}

		/// <summary>
		/// Postprocesser to convert an <see cref="Entity"/> from a Quake 2-based BSP to one for Hammer.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to parse.</param>
		private void PostProcessQuake2Entity(Entity entity) {
			if (!entity["angle"].Equals("")) {
				entity["angles"] = "0 " + entity["angle"] + " 0";
				entity.Remove("angle");
			}

			switch (entity["classname"].ToLower()) {
				case "func_wall": {
					entity["classname"] = "func_brush";
					// 2 I believe is "Start enabled" and 4 is "toggleable", or the other way around. Not sure.
					if (entity.SpawnflagsSet(2) || entity.SpawnflagsSet(4)) {
						entity["solidity"] = "0";
					} else {
						entity["solidity"] = "2";
					}
					break;
				}
				case "info_player_start":
				case "info_player_deathmatch": {
					Vector3 origin = entity.Origin;
					entity.Origin = new Vector3(origin.X, origin.Y, (origin.Z + 18));
					break;
				}
				case "light": {
					Vector4 color;
					if (entity.ContainsKey("_color")) {
						color = entity.GetVector("_color");
					} else {
						color = Vector4.One;
					}
					color *= 255;
					float intensity = entity.GetFloat("light", 1);
					entity.Remove("_color");
					entity.Remove("light");
					entity["_light"] = color.X + " " + color.Y + " " + color.Z + " " + intensity;
					break;
				}
				case "misc_teleporter": {
					Vector3 origin = entity.Origin;
					Vector3 mins = new Vector3(origin.X - 24, origin.Y - 24, origin.Z - 24);
					Vector3 maxs = new Vector3(origin.X + 24, origin.Y + 24, origin.Z + 48);
					entity.brushes.Add(MAPBrushExtensions.CreateCube(mins, maxs, "tools/toolstrigger"));
					entity.Remove("origin");
					entity["classname"] = "trigger_teleport";
					break;
				}
				case "misc_teleporter_dest": {
					entity["classname"] = "info_target";
					break;
				}
			}
		}

		/// <summary>
		/// Postprocesser to convert an <see cref="Entity"/> from a Quake 3-based BSP to one for Hammer.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to parse.</param>
		private void PostProcessQuake3Entity(Entity entity) {
			if (!entity["angle"].Equals("")) {
				entity["angles"] = "0 " + entity["angle"] + " 0";
				entity.Remove("angle");
			}

			switch (entity["classname"].ToLower()) {
				case "light": {
					Vector4 color;
					if (entity.ContainsKey("_color")) {
						color = entity.GetVector("_color");
					} else {
						color = Vector4.One;
					}
					color *= 255;
					float intensity = entity.GetFloat("light", 1);
					entity.Remove("_color");
					entity.Remove("light");
					entity["_light"] = color.X + " " + color.Y + " " + color.Z + " " + intensity;
					break;
				}
				case "func_rotatingdoor": {
					entity.ClassName = "func_door_rotating";
					break;
				}
			}
		}

		/// <summary>
		/// Turn a triggering entity (like a func_button or trigger_multiple) into a Source
		/// engine trigger using entity I/O. There's a few complications to this: There's
		/// no generic output which always acts like the triggers in other engines, and there's
		/// no "Fire" input. I try to figure out which ones are best based on their classnames
		/// but it's not 100% foolproof, and I have to add a case for every specific class.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to parse I/O connections for.</param>
		public void ParseEntityIO(Entity entity) {
			if (!(entity["target"] == "")) {
				float delay = entity.GetFloat("delay", 0.0f);
				if (!entity["target"].Equals("")) {
					Entity[] targets = GetTargets(entity["target"]);
					foreach (Entity target in targets) {
						if (target.ValueIs("classname", "multi_manager") || target.ValueIs("classname", "multi_kill_manager")) {
							Entity mm = ParseMultimanager(target);
							foreach (Entity.EntityConnection connection in mm.connections) {
								if (entity.ValueIs("classname", "logic_relay") && entity.ContainsKey("delay")) {
									entity.connections.Add(new Entity.EntityConnection() { name = "OnTrigger", target = connection.target, action = connection.action, param = connection.param, delay = connection.delay + delay, fireOnce = connection.fireOnce, unknown0 = "", unknown1 = "" });
								} else {
									entity.connections.Add(new Entity.EntityConnection() { name = entity.FireAction(), target = connection.target, action = connection.action, param = connection.param, delay = connection.delay, fireOnce = connection.fireOnce, unknown0 = "", unknown1 = "" });
								}
							}
						} else {
							string outputAction = target.OnFire();
							if (entity.ValueIs("triggerstate", "0")) {
								outputAction = target.OnDisable();
							} else {
								if (entity.ValueIs("triggerstate", "1")) {
									outputAction = target.OnEnable();
								}
							}
							entity.connections.Add(new Entity.EntityConnection() { name = entity.FireAction(), target = target["targetname"], action = outputAction, param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
						}
					}
				}
				if (!entity["killtarget"].Equals("")) {
					entity.connections.Add(new Entity.EntityConnection() { name = entity.FireAction(), target = entity["killtarget"], action = "Kill", param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
				}
				entity.Remove("target");
				entity.Remove("killtarget");
				entity.Remove("triggerstate");
				entity.Remove("delay");
			}
		}

		/// <summary>
		/// Multimanagers are also a special case. There are none in Source. Instead, I
		/// need to add EVERY targetted entity in a multimanager to the original trigger
		/// entity as an output with the specified delay. Things get even more complicated
		/// when a multi_manager fires another multi_manager. In this case, this method will
		/// recurse on itself until all the complexity is worked out.
		/// One potential problem is if two multi_managers continuously call each other, this
		/// method will recurse infinitely until there is a stack overflow. This might happen
		/// when there is some sort of cycle going on in the map and multi_managers call each
		/// other recursively to run the cycle with a delay. I solve this with an atrificial
		/// limit of 8 multimanager recursions.
		/// TODO: It would be better to detect this problem when it happens.
		/// TODO: Instead of adding more attributes, parse into connections.
		/// </summary>
		/// <param name="entity">The multi_manager to parse.</param>
		/// <returns>The parsed multi_manager. This will have all targets as <see cref="Entity.EntityConnection"/> objects.</returns>
		private Entity ParseMultimanager(Entity entity) {
			++_mmStackLength;
			Entity dummy = new Entity(entity);
			dummy.Remove("classname");
			dummy.Remove("origin");
			dummy.Remove("angles");
			dummy.Remove("targetname");
			List<string> delete = new List<string>();
			foreach (KeyValuePair<string, string> kvp in dummy) {
				string targetname = kvp.Key;
				float delay = dummy.GetFloat(kvp.Key, 0.0f);
				for (int i = targetname.Length - 1; i >= 0; --i) {
					if (targetname[i] == '#') {
						targetname = targetname.Substring(0, i);
						break;
					}
				}
				Entity[] targets = GetTargets(targetname);
				delete.Add(kvp.Key);
				for (int i = 0; i < targets.Length; i++) {
					if (entity.ValueIs("classname", "multi_kill_manager")) {
						if (targets.Length > 1) {
							dummy.connections.Add(new Entity.EntityConnection() { name = "condition", target = targetname + i, action = "Kill", param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
						} else {
							dummy.connections.Add(new Entity.EntityConnection() { name = "condition", target = targetname, action = "Kill", param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
						}
					} else {
						if (targets[i].ValueIs("classname", "multi_manager") || targets[i].ValueIs("classname", "multi_kill_manager")) {
							if (_mmStackLength <= 8) {
							//if (_mmStackLength <= Settings.MMStackSize) {
								Entity mm = ParseMultimanager(targets[i]);
								foreach (Entity.EntityConnection connection in mm.connections) {
									dummy.connections.Add(new Entity.EntityConnection() { name = connection.name, target = connection.target, action = connection.action, param = connection.param, delay = connection.delay + delay, fireOnce = connection.fireOnce, unknown0 = connection.unknown0, unknown1 = connection.unknown1 });
								}
							} else {
								_master.Print("WARNING: Multimanager stack overflow on entity " + entity["targetname"] + " calling " + targets[i]["targetname"] + "!");
								_master.Print("This is probably because of multi_managers repeatedly calling eachother.");
							}
						} else {
							if (targets.Length > 1) {
								string outputAction = targets[i].OnFire();
								if (entity.ValueIs("triggerstate", "0")) {
									outputAction = targets[i].OnDisable();
								} else {
									if (entity.ValueIs("triggerstate", "1")) {
										outputAction = targets[i].OnEnable();
									}
								}
								dummy.connections.Add(new Entity.EntityConnection() { name = "condition", target = targetname + i, action = outputAction, param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
							} else if (targets.Length == 1) {
								string outputAction = targets[0].OnFire();
								if (entity.ValueIs("triggerstate", "0")) {
									outputAction = targets[0].OnDisable();
								} else {
									if (entity.ValueIs("triggerstate", "1")) {
										outputAction = targets[0].OnEnable();
									}
								}
								dummy.connections.Add(new Entity.EntityConnection() { name = "condition", target = targetname, action = outputAction, param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
							} else {
								dummy.connections.Add(new Entity.EntityConnection() { name = "condition", target = targetname, action = "Toggle", param = "", delay = delay, fireOnce = -1, unknown0 = "", unknown1 = "" });
							}
						}
					}
				}
			}
			foreach (string st in delete) {
				dummy.Remove(st);
			}
			--_mmStackLength;
			return dummy;
		}

		/// <summary>
		/// Since Source also requires explicit enable/disable on/off events (and many
		/// entities don't support the "Toggle" input) I can't have multiple entities
		/// with the same targetname. So these need to be distinguished and tracked.
		/// </summary>
		/// <param name="name">The targetname of entities to get.</param>
		/// <returns>An array of all <see cref="Entity"/> objects with targetname set to <paramref name="name"/> if they have unique FireActions, or an array of one <see cref="Entity"/> if all the FireAcitons are the same.</returns>
		private Entity[] GetTargets(string name) {
			bool numeralized = false;
			List<Entity> targets = new List<Entity>();
			int numNumeralized = 0;
			//foreach (string numeralizedTargetname in _numeralizedTargetnames) {
			for (int i = 0; i < _numeralizedTargetnames.Count; ++i) {
				if (_numeralizedTargetnames[i].Equals(name)) {
					numeralized = true;
					numNumeralized = _numTargets[i];
					break;
				}
			}
			if (numeralized) {
				targets = new List<Entity>(numNumeralized);
				for (int i = 0; i < numNumeralized; ++i) {
					targets.Add(_entities.GetWithName(name + i));
				}
			} else {
				targets = _entities.GetAllWithName(name);
				if (targets.Count > 1) {
					// Make sure each target needs its own Fire action and name
					bool unique = false;
					for (int i = 1; i < targets.Count; ++i) {
						if (!targets[0].OnFire().Equals(targets[i].OnFire())) {
							unique = true;
							break;
						}
					}
					if (!unique) {
						return new Entity[] { targets[0] };
					}
					_numeralizedTargetnames.Add(name);
					_numTargets.Add(targets.Count);
					for (int i = 0; i < targets.Count; ++i) {
						targets[i]["targetname"] = name + i;
					}
				}
			}
			return targets.ToArray<Entity>();
		}

		/// <summary>
		/// Every <see cref="MAPBrushSide"/> contained in <paramref name="brushes"/> will have its texture examined,
		/// and, if necessary, replaced with the equivalent for Hammer.
		/// </summary>
		/// <param name="brushes">The collection of <see cref="MAPBrush"/> objects to have textures parsed.</param>
		/// <param name="version">The <see cref="MapType"/> of the BSP this entity came from.</param>
		private void PostProcessTextures(IEnumerable<MAPBrush> brushes) {
			foreach (MAPBrush brush in brushes) {
				foreach (MAPBrushSide brushSide in brush.sides) {
					brushSide.textureInfo.Validate(brushSide.plane);
					PostProcessSpecialTexture(brushSide);

					if (_version == MapType.Nightfire) {
						PostProcessNightfireTexture(brushSide);
					} else if (_version.IsSubtypeOf(MapType.Quake2)) {
						PostProcessQuake2Texture(brushSide);
					} else if (_version.IsSubtypeOf(MapType.Source)) {
						PostProcessSourceTexture(brushSide);
					} else if (_version.IsSubtypeOf(MapType.Quake3)) {
						PostProcessQuake3Texture(brushSide);
					}
				}
			}
		}

		/// <summary>
		/// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by GTKRadiant, if necessary.
		/// These textures are produced by the decompiler algorithm itself.
		/// </summary>
		/// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
		private void PostProcessSpecialTexture(MAPBrushSide brushSide) {
			switch (brushSide.texture.ToLower()) {
				case "**nulltexture**":
				case "**nodrawtexture**": {
					brushSide.texture = "tools/toolsnodraw";
					break;
				}
				case "**skiptexture**": {
					brushSide.texture = "tools/toolsskip";
					break;
				}
				case "**skytexture**": {
					brushSide.texture = "tools/toolsskybox";
					break;
				}
				case "**hinttexture**": {
					brushSide.texture = "tools/toolshint";
					break;
				}
				case "**cliptexture**": {
					brushSide.texture = "tools/toolsclip";
					break;
				}
			}
		}

		/// <summary>
		/// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Hammer, if necessary.
		/// </summary>
		/// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
		private void PostProcessNightfireTexture(MAPBrushSide brushSide) {
			switch (brushSide.texture.ToLower()) {
				case "special/nodraw":
				case "special/null": {
					brushSide.texture = "tools/toolsnodraw";
					break;
				}
				case "special/clip": {
					brushSide.texture = "tools/toolsclip";
					break;
				}
				case "special/sky": {
					brushSide.texture = "tools/toolsskybox";
					break;
				}
				case "special/trigger": {
					brushSide.texture = "tools/toolstrigger";
					break;
				}
				case "special/playerclip": {
					brushSide.texture = "tools/toolsplayerclip";
					break;
				}
				case "special/npcclip":
				case "special/enemyclip": {
					brushSide.texture = "tools/toolsnpcclip";
					break;
				}
			}
		}

		/// <summary>
		/// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Hammer, if necessary.
		/// </summary>
		/// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
		private void PostProcessQuake2Texture(MAPBrushSide brushSide) {
			if (brushSide.texture.Length >= 5 && brushSide.texture.Substring(brushSide.texture.Length - 5).Equals("/clip", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = "tools/toolsclip";
			} else if (brushSide.texture.Length >= 5 && brushSide.texture.Substring(brushSide.texture.Length - 5).Equals("/hint", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = "tools/toolshint";
			} else if (brushSide.texture.Length >= 8 && brushSide.texture.Substring(brushSide.texture.Length - 8).Equals("/trigger", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = "tools/toolstrigger";
			} else if (brushSide.texture.Equals("*** unsused_texinfo ***", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = "tools/toolsnodraw";
			}
		}

		/// <summary>
		/// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Hammer, if necessary.
		/// </summary>
		/// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
		private void PostProcessQuake3Texture(MAPBrushSide brushSide) {
			if (brushSide.texture.Length >= 9 && brushSide.texture.Substring(0, 9).Equals("textures/", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = brushSide.texture.Substring(9);
			}
			switch (brushSide.texture.ToLower()) {
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
				case "common/clip": {
					brushSide.texture = "tools/toolsclip";
					break;
				}
				case "common/nodrawnonsolid":
				case "system/trigger":
				case "common/trigger": {
					brushSide.texture = "tools/toolstrigger";
					break;
				}
				case "common/nodraw":
				case "common/caulkshadow":
				case "common/caulk":
				case "system/caulk":
				case "noshader": {
					brushSide.texture = "tools/toolsnodraw";
					break;
				}
				case "common/do_not_enter":
				case "common/donotenter":
				case "common/monsterclip": {
					brushSide.texture = "tools/toolsnpcclip";
					break;
				}
				case "common/caulksky":
				case "common/skyportal": {
					brushSide.texture = "tools/toolsskybox";
					break;
				}
				case "common/hint": {
					brushSide.texture = "tools/toolshint";
					break;
				}
				case "common/waterskip": {
					brushSide.texture = "liquids/!water";
					break;
				}
				case "system/do_not_enter":
				case "common/playerclip": {
					brushSide.texture = "tools/toolsplayerclip";
					break;
				}
			}
			if (brushSide.texture.Length >= 4 && brushSide.texture.Substring(0, 4).Equals("sky/", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = "tools/toolsskybox";
			}
		}

		/// <summary>
		/// Postprocesser to convert the texture referenced by <paramref name="brushSide"/> into one used by Hammer, if necessary.
		/// </summary>
		/// <param name="brushSide">The <see cref="MAPBrushSide"/> to have its texture parsed.</param>
		private void PostProcessSourceTexture(MAPBrushSide brushSide) {
			if (brushSide.texture.Length >= 5 && brushSide.texture.Substring(0, 5).Equals("maps/", StringComparison.InvariantCultureIgnoreCase)) {
				brushSide.texture = brushSide.texture.Substring(5);
				for (int i = 0; i < brushSide.texture.Length; ++i) {
					if (brushSide.texture[i] == '/') {
						brushSide.texture = brushSide.texture.Substring(i + 1);
						break;
					}
				}
			}

			// Parse cubemap textures
			// I'm sure this could be done more concisely with regex, but I suck at regex.
			int numUnderscores = 0;
			bool validnumber = false;
			for (int i = brushSide.texture.Length - 1; i > 0; --i) {
				if (brushSide.texture[i] <= '9' && brushSide.texture[i] >= '0') {
					// Current is a number, this may be a cubemap reference
					validnumber = true;
				} else {
					if (brushSide.texture[i] == '-') {
						// Current is a minus sign (-).
						if (!validnumber) {
							break; // Make sure there's a number to add the minus sign to. If not, kill the loop.
						}
					} else {
						if (brushSide.texture[i] == '_') {
							// Current is an underscore (_)
							if (validnumber) {
								// Make sure there is a number in the current string
								++numUnderscores; // before moving on to the next one.
								if (numUnderscores == 3) {
									// If we've got all our numbers
									brushSide.texture = brushSide.texture.Substring(0, i); // Cut the texture string
									break; // Kill the loop, we're done
								}
								validnumber = false;
							} else {
								// No number after the underscore
								break;
							}
						} else {
							// Not an acceptable character
							break;
						}
					}
				}
			}

		}

	}
}
