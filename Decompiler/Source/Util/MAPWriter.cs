using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;
using System.Runtime.Serialization.Formatters.Binary;

using LibBSP;
using System.Reflection;

namespace Decompiler {
	/// <summary>
	/// Class for orchestrating entity postprocessing and file output for BSP decompiles.
	/// </summary>
	public class MAPWriter {

		private Job _master;

		private Entities _entities;
		private MapType _version;
		private string _mapName;
		private string _mapDirectory;
		 
		/// <summary>
		/// Creates a new instance of a <see cref="MAPWriter"/> object.
		/// </summary>
		/// <param name="entities">The <see cref="Entities"/> resulting from a decompile.</param>
		/// <param name="mapDirectory">The directory in which the original map file resides.</param>
		/// <param name="mapName">The name of the map.</param>
		/// <param name="version">The version of BSP this was.</param>
		/// <param name="master">The parent <see cref="Job"/> object for this instance.</param>
		public MAPWriter(Entities entities, string mapDirectory, string mapName, MapType version, Job master) {
			_entities = entities;
			_version = version;
			_mapName = mapName;
			_mapDirectory = mapDirectory;
			_master = master;
		}

		/// <summary>
		/// Postprocesses entities and writes all desired mapfiles.
		/// </summary>
		public void WriteAll() {
			if (_master.settings.toAuto) {
				if (_version.IsSubtypeOf(MapType.Source)) {
					WriteHammer();
				} else if (_version == MapType.Nightfire) {
					WriteGearcraft();
				} else if (_version.IsSubtypeOf(MapType.MOHAA)) {
					WriteMoHRadiant();
				} else if (_version.IsSubtypeOf(MapType.CoD)) {
					WriteCoDRadiant();
				} else if (_version.IsSubtypeOf(MapType.Quake3)) {
					WriteRadiant();
				} else {
					throw new NotImplementedException("No default output format specified for map type " + _version.ToString());
				}
			} else {
				if (_master.settings.toM510) {
					WriteGearcraft(_master.settings.toDoomEdit || _master.settings.toGTK || _master.settings.toMoH || _master.settings.toVMF || _master.settings.toCoD);
				}
				if (_master.settings.toMoH) {
					WriteMoHRadiant(_master.settings.toDoomEdit || _master.settings.toGTK || _master.settings.toVMF || _master.settings.toCoD);
				}
				if (_master.settings.toGTK) {
					WriteRadiant(_master.settings.toDoomEdit || _master.settings.toVMF || _master.settings.toCoD);
				}
				if (_master.settings.toDoomEdit) {
					WriteDoomEdit(_master.settings.toVMF || _master.settings.toCoD);
				}
				if (_master.settings.toCoD) {
					WriteCoDRadiant(_master.settings.toVMF);
				}
				if (_master.settings.toVMF) {
					WriteHammer();
				}
			}
		}

		/// <summary>
		/// Writes a GearCraft map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteGearcraft(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToGearcraft entityPostProcessor = new EntityToGearcraft(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			GearcraftMapGenerator mapMaker = new GearcraftMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = deepCopy ? "_gc.map" : ".map";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for Gearcraft");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for Gearcraft");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Writes a MoHRadiant map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteMoHRadiant(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToMoHRadiant entityPostProcessor = new EntityToMoHRadiant(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			MoHRadiantMapGenerator mapMaker = new MoHRadiantMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = deepCopy ? "_moh.map" : ".map";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for MoHRadiant");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for MoHRadiant");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Writes a CoDRadiant map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteCoDRadiant(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToCoDRadiant entityPostProcessor = new EntityToCoDRadiant(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			CoDRadiantMapGenerator mapMaker = new CoDRadiantMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = deepCopy ? "_cod.map" : ".map";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for CoDRadiant");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for CoDRadiant");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Writes a Radiant map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteRadiant(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToRadiant entityPostProcessor = new EntityToRadiant(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			RadiantMapGenerator mapMaker = new RadiantMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = deepCopy ? "_radiant.map" : ".map";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for Radiant");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for Radiant");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Writes a DoomEdit map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteDoomEdit(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToDoomEdit entityPostProcessor = new EntityToDoomEdit(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			DoomEditMapGenerator mapMaker = new DoomEditMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = deepCopy ? "_doom.map" : ".map";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for DoomEdit");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for DoomEdit");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Writes a Hammer map using the provided <see cref="Entities"/>.
		/// </summary>
		/// <param name="deepCopy">If <c>true</c>, the <see cref="Entities"/> will be deep copied to preserve the original ones, so postprocessing for this format won't interfere with others.</param>
		public void WriteHammer(bool deepCopy = false) {
			Entities myEntities = deepCopy ? (Entities)DeepCopy(_entities) : _entities;
			EntityToHammer entityPostProcessor = new EntityToHammer(myEntities, _version, _master);
			entityPostProcessor.PostProcessEntities();
			HammerMapGenerator mapMaker = new HammerMapGenerator(myEntities, _master);
			string output = mapMaker.ParseMap();

			string extension = ".vmf";
			if (string.IsNullOrEmpty(_master.settings.outputFolder)) {
				_master.Print("Writing file " + Path.Combine(_mapDirectory, _mapName + extension) + " for Hammer");
				File.WriteAllText(Path.Combine(_mapDirectory, _mapName + extension), output);
			} else {
				_master.Print("Writing file " + Path.Combine(_master.settings.outputFolder, _mapName + extension) + " for Hammer");
				File.WriteAllText(Path.Combine(_master.settings.outputFolder, _mapName + extension), output);
			}
		}

		/// <summary>
		/// Performs a basic deep copy through reflection.
		/// </summary>
		public static object DeepCopy(object obj) {
			if (obj == null) {
				return null;
			}
			Type type = obj.GetType();

			if (type.IsValueType || type == typeof(string)) {
				return obj;
			} else if (type.IsArray) {
				Type elementType = type.GetElementType();
				Array array = obj as Array;
				Array copied = Array.CreateInstance(elementType, array.Length);
				for (int i = 0; i < array.Length; i++) {
					copied.SetValue(DeepCopy(array.GetValue(i)), i);
				}
				return Convert.ChangeType(copied, type);
			} else if (type.IsClass) {
				if (type.GetConstructor(new Type[] { }) == null) {
					return obj;
				}
				object instance = Activator.CreateInstance(type);
				Type currentType = instance.GetType();
				while (currentType != null) {
					// FlattenHierarchy does not work with NonPublic.
					FieldInfo[] fields = currentType.GetFields(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance);
					foreach (FieldInfo field in fields) {
						object fieldValue = field.GetValue(obj);
						if (fieldValue == null) {
							continue;
						}
						if (currentType.Name == "Lump`1" && field.Name == "<Bsp>k__BackingField") {
							field.SetValue(instance, fieldValue);
						} else if (currentType.Name == "Entity" && field.Name == "<Parent>k__BackingField") {
							field.SetValue(instance, fieldValue);
						} else {
							field.SetValue(instance, DeepCopy(fieldValue));
						}
					}

					currentType = currentType.BaseType;
				}
				return instance;
			} else {
				throw new ArgumentException("Unable to DeepCopy type " + type.Name + ".");
			}
		}

	}
}
