using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Class for decompiling the data in a <see cref="BSP"/> object.
	/// </summary>
	public class BSPDecompiler {

		private Job _master;

		private BSP _bsp;
		private int _currentSideIndex = 0;
		private int _itemsToProcess = 0;
		private int _itemsProcessed = 0;

		/// <summary>
		/// Creates a new instance of a <see cref="Decompiler"/> object.
		/// </summary>
		/// <param name="bsp">The <see cref="BSP"/> object which will be processed.</param>
		/// <param name="master">The parent <see cref="Job"/> object for this instance.</param>
		public BSPDecompiler(BSP bsp, Job master) {
			this._bsp = bsp;
			this._master = master;

			if (bsp.Entities != null) { _itemsToProcess += bsp.Entities.Count; }
			if (bsp.Brushes != null) { _itemsToProcess += bsp.Brushes.Count; }
			if (bsp.StaticProps != null) { _itemsToProcess += bsp.StaticProps.Count; }
			if (bsp.StaticModels != null) { _itemsToProcess += bsp.StaticModels.Count; }
			if (bsp.Cubemaps != null) { _itemsToProcess += bsp.Cubemaps.Count; }
		}

		/// <summary>
		/// Begins the decompiling process on the <see cref="BSP"/> object passed to the constructor.
		/// </summary>
		/// <returns>An <see cref="Entities"/> object containing all the processed data.</returns>
		public Entities Decompile() {
			// There's no need to deepcopy; only one process will run on these entities and this will not be saved back to a BSP file
			Entities entities = _bsp.Entities;
			foreach (Entity entity in entities) {
				ProcessEntity(entity);
				++_itemsProcessed;
				ReportProgress();
			}
			if (_bsp.StaticProps != null) {
				foreach (StaticProp prop in _bsp.StaticProps) {
					entities.Add(prop.ToEntity(_bsp.StaticProps.ModelDictionary));
					++_itemsProcessed;
					ReportProgress();
				}
			}
			if (_bsp.StaticModels != null) {
				foreach (StaticModel model in _bsp.StaticModels) {
					entities.Add(model.ToEntity());
					++_itemsProcessed;
					ReportProgress();
				}
			}
			if (_bsp.Cubemaps != null) {
				foreach (Cubemap cubemap in _bsp.Cubemaps) {
					entities.Add(cubemap.ToEntity());
					++_itemsProcessed;
					ReportProgress();
				}
			}

			return entities;
		}

		/// <summary>
		/// Processes an <see cref="Entity"/> into a state where it can be output into a file that map editors can read.
		/// </summary>
		/// <param name="entity">The <see cref="Entity"/> to process.</param>
		/// <remarks>This method does not return anything, since the <see cref="Entity"/> object is modified by reference.</remarks>
		private void ProcessEntity(Entity entity) {
			int modelNumber = entity.ModelNumber;
			// If this Entity has no modelNumber, then this is a no-op. No processing is needed.
			// A modelnumber of 0 indicates the world entity.
			if (modelNumber >= 0) {
				Model model = _bsp.Models[modelNumber];

				if (_bsp.Brushes != null) {
					List<Brush> brushes = _bsp.GetBrushesInModel(model);
					if (brushes != null) {
						foreach (Brush brush in brushes) {
							MAPBrush result = ProcessBrush(brush, entity.Origin);
							result.isWater |= (entity.ClassName == "func_water");
							if (_master.settings.brushesToWorld) {
								_bsp.Entities.GetWithAttribute("classname", "worldspawn").brushes.Add(result);
							} else {
								entity.brushes.Add(result);
							}
							++_itemsProcessed;
							ReportProgress();
						}
					}
				}

				if (model.NumPatchIndices > 0 && _bsp.LeafFaces != null && _bsp.Patches != null) {
					HashSet<Patch> patches = new HashSet<Patch>();
					List<long> leafPatchesInModel = _bsp.GetReferencedObjects<long>(model, "PatchIndices");
					foreach (long leafPatch in leafPatchesInModel) {
						if (leafPatch >= 0) {
							patches.Add(_bsp.Patches[(int)leafPatch]);
						}
					}
					foreach (Patch patch in patches) {
						if (_bsp.MapType != MapType.CoD || patch.Type == 0) {
							MAPPatch mappatch = ProcessPatch(patch);
							MAPBrush newBrush = new MAPBrush();
							newBrush.patch = mappatch;
							entity.brushes.Add(newBrush);
						}
					}
				}

				if (_bsp.Faces != null) {
					List<Face> surfaces = _bsp.GetFacesInModel(model);
					foreach (Face face in surfaces) {
						if (face.DisplacementIndex >= 0) {
							if (modelNumber != 0) {
								_master.Print("WARNING: Displacement not part of world in " + _bsp.MapName);
							}
							MAPDisplacement displacement = ProcessDisplacement(_bsp.Displacements[face.DisplacementIndex]);
							MAPBrush newBrush = face.CreateBrush(_bsp, 32);
							newBrush.sides[0].displacement = displacement;
							// If we are not decompiling to VMF, vis will need to skip this brush.
							newBrush.isDetail = true;
							entity.brushes.Add(newBrush);
						} else if (face.Type == 2) {
							MAPPatch patch = ProcessPatch(face);
							MAPBrush newBrush = new MAPBrush();
							newBrush.patch = patch;
							entity.brushes.Add(newBrush);
						} else if (_bsp.MapType.IsSubtypeOf(MapType.STEF2) && face.Type == 5) {
							if (modelNumber != 0) {
								_master.Print("WARNING: Terrain not part of world in " + _bsp.MapName);
							}
							MAPTerrainEF2 terrain = ProcessEF2Terrain(face);
							MAPBrush newBrush = new MAPBrush();
							newBrush.ef2Terrain = terrain;
							entity.brushes.Add(newBrush);
						}
					}
				}

				// If this is model 0 (worldspawn) there are other things that need to be taken into account.
				if (modelNumber == 0) {
					if (_bsp.LODTerrains != null) {
						foreach (LODTerrain lodTerrain in _bsp.LODTerrains) {
							MAPTerrainMoHAA terrain = ProcessTerrainMoHAA(lodTerrain);
							MAPBrush newBrush = new MAPBrush();
							newBrush.mohTerrain = terrain;
							entity.brushes.Add(newBrush);
						}
					}
				}
				entity.Remove("model");
			}
		}

		/// <summary>
		/// Processes a <see cref="Brush"/> into a state where it can be output into a file that map editors can read.
		/// </summary>
		/// <param name="brush">The <see cref="Brush"/> to process.</param>
		/// <param name="worldPosition">The position of the parent <see cref="Entity"/> in the world. This is important for calculating UVs on solids.</param>
		/// <returns>The processed <see cref="MAPBrush"/> object, to be added to an <see cref="Entity"/> object.</returns>
		private MAPBrush ProcessBrush(Brush brush, Vector3 worldPosition) {
			List<BrushSide> sides;
			// CoD BSPs store brush sides sequentially so the brush structure doesn't reference a first side.
			if (brush.FirstSideIndex < 0) {
				sides = _bsp.BrushSides.GetRange(_currentSideIndex, brush.NumSides);
				_currentSideIndex += brush.NumSides;
			} else {
				sides = _bsp.GetReferencedObjects<BrushSide>(brush, "BrushSides");
			}
			MAPBrush mapBrush = new MAPBrush();
			mapBrush.isDetail = brush.IsDetail(_bsp);
			mapBrush.isWater = brush.IsWater(_bsp);
			mapBrush.isManVis = brush.IsManVis(_bsp);
			int sideNum = 0;
			foreach (BrushSide side in sides) {
				MAPBrushSide mapBrushSide = ProcessBrushSide(side, worldPosition, sideNum);
				if (mapBrushSide != null) {
					mapBrush.sides.Add(mapBrushSide);
				}
				++sideNum;
			}

			return mapBrush;
		}

		/// <summary>
		/// Processes a <see cref="BrushSide"/> into a state where it can be output into a file that map editors can read.
		/// </summary>
		/// <param name="brushSide">The <see cref="BrushSide"/> to process.</param>
		/// <param name="worldPosition">The position of the parent <see cref="Entity"/> in the world. This is important for calculating UVs on solids.</param>
		/// <param name="sideIndex">The index of this side reference in the parent <see cref="Brush"/>. Important for Call of Duty series maps, since
		/// the first six <see cref="BrushSide"/>s in a <see cref="Brush"/> don't contain <see cref="Plane"/> references.</param>
		/// <returns>The processed <see cref="MAPBrushSode"/> object, to be added to a <see cref="Brush"/> object.</returns>
		private MAPBrushSide ProcessBrushSide(BrushSide brushSide, Vector3 worldPosition, int sideIndex) {
			if (brushSide.IsBevel) { return null; }
			MAPBrushSide mapBrushSide;
			// The things we'll need to define a .MAP brush side
			string texture;
			string material = "wld_lightmap";
			TextureInfo texInfo;
			Vector3[] threePoints;
			Plane plane;
			int flags = 0;

			// If we have a face reference here, let's use it!
			if (brushSide.FaceIndex >= 0) {
				Face face = _bsp.Faces[brushSide.FaceIndex];
				// In Nightfire, faces with "256" flag set should be ignored
				if ((face.Type & (1 << 8)) != 0) { return null; }
				texture = (_master.settings.replace512WithNull && (face.Type & (1 << 9)) != 0) ? "**nulltexture**" : _bsp.Textures[face.TextureIndex].Name;
				threePoints = GetPointsForFace(face, brushSide);
				if (face.PlaneIndex >= 0 && face.PlaneIndex < _bsp.Planes.Count) {
					plane = _bsp.Planes[face.PlaneIndex];
				} else if (brushSide.PlaneIndex >= 0 && brushSide.PlaneIndex < _bsp.Planes.Count) {
					plane = _bsp.Planes[brushSide.PlaneIndex];
				} else {
					plane = new Plane(0, 0, 0, 0);
				}
				if (_bsp.TextureInfo != null) {
					texInfo = _bsp.TextureInfo[face.TextureInfoIndex];
				} else {
					Vector3[] newAxes = TextureInfo.TextureAxisFromPlane(plane);
					texInfo = new TextureInfo(newAxes[0], newAxes[1], Vector2.Zero, Vector2.One, flags, -1, 0);
				}
				flags = _master.settings.noFaceFlags ? 0 : face.Type;
				if (face.MaterialIndex >= 0) {
					material = _bsp.Materials[face.MaterialIndex].Name;
				}
			} else {
				if (_bsp.MapType.IsSubtypeOf(MapType.CoD)) {
					switch (sideIndex) {
						case 0: { // XMin
							plane = new Plane(-1, 0, 0, -brushSide.Distance);
							break;
						}
						case 1: { // XMax
							plane = new Plane(1, 0, 0, brushSide.Distance);
							break;
						}
						case 2: { // YMin
							plane = new Plane(0, -1, 0, -brushSide.Distance);
							break;
						}
						case 3: { // YMax
							plane = new Plane(0, 1, 0, brushSide.Distance);
							break;
						}
						case 4: { // ZMin
							plane = new Plane(0, 0, -1, -brushSide.Distance);
							break;
						}
						case 5: { // ZMax
							plane = new Plane(0, 0, 1, brushSide.Distance);
							break;
						}
						default: {
							plane = _bsp.Planes[brushSide.PlaneIndex];
							break;
						}
					}
				} else {
					plane = _bsp.Planes[brushSide.PlaneIndex];
				}
				threePoints = plane.GenerateThreePoints();
				if (brushSide.TextureIndex >= 0) {
					if (_bsp.MapType.IsSubtypeOf(MapType.Source)) {
						texInfo = _bsp.TextureInfo[brushSide.TextureIndex];
						TextureData currentTexData;
						// I've only found one case where this is bad: c2a3a in HL Source. Don't know why.
						if (texInfo.TextureIndex >= 0) {
							currentTexData = _bsp.TextureData[texInfo.TextureIndex];
							texture = _bsp.Textures.GetTextureAtOffset((uint)_bsp.TextureTable[currentTexData.TextureStringOffsetIndex]);
						} else {
							texture = "**skiptexture**";
						}
					} else {
						Texture textureDef = _bsp.Textures[brushSide.TextureIndex];
						texture = textureDef.Name;
						texInfo = textureDef.TextureInfo;
					}
				} else {
					Vector3[] newAxes = TextureInfo.TextureAxisFromPlane(plane);
					texInfo = new TextureInfo(newAxes[0], newAxes[1], Vector2.Zero, Vector2.One, flags, -1, 0);
					texture = "**cliptexture**";
				}
			}

			TextureInfo outputTexInfo;
			if (texInfo.Data != null && texInfo.Data.Length > 0) {
				outputTexInfo = texInfo.BSP2MAPTexInfo(worldPosition);
			} else {
				Vector3[] newAxes = TextureInfo.TextureAxisFromPlane(plane);
				outputTexInfo = new TextureInfo(newAxes[0], newAxes[1], Vector2.Zero, Vector2.One, 0, -1, 0);
			}

			mapBrushSide = new MAPBrushSide() {
				vertices = threePoints,
				plane = plane,
				texture = texture,
				textureInfo = outputTexInfo,
				material = material,
				lgtScale = 16,
				lgtRot = 0
			};

			return mapBrushSide;
		}

		/// <summary>
		/// Processes a <see cref="Face"/> as a biquadratic Bezier patch. The vertices of the <see cref="Face"/> are interpreted
		/// as control points for multiple spline curves, which are then interpolated and rendered at an arbitrary quality value.
		/// For Quake 3 engine forks only.
		/// </summary>
		/// <param name="face">The <see cref="Face"/> object to process.</param>
		/// <returns>A <see cref="MAPPatch"/> object to be added to a <see cref="MAPBrush"/> object.</returns>
		private MAPPatch ProcessPatch(Face face) {
			List<Vector3> vertices = _bsp.GetReferencedObjects<Vector3>(face, "Vertices");
			return new MAPPatch() {
				dims = face.PatchSize,
				texture = _bsp.Textures[face.TextureIndex].Name,
				points = vertices.ToArray<Vector3>()
			};
		}

		/// <summary>
		/// Processes a <see cref="Patch"/> into a <see cref="MAPPatch"/>. The vertices of the <see cref="Patch"/> are interpreted
		/// as control points for multiple spline curves, which are then interpolated and rendered at an arbitrary quality value.
		/// For Call of Duty engine forks only.
		/// </summary>
		/// <param name="face">The <see cref="Patch"/> object to process.</param>
		/// <returns>A <see cref="MAPPatch"/> object to be added to a <see cref="MAPBrush"/> object.</returns>
		private MAPPatch ProcessPatch(Patch patch) {
			List<Vector3> vertices = _bsp.GetReferencedObjects<Vector3>(patch, "PatchVertices");
			return new MAPPatch() {
				dims = patch.Dimensions,
				texture = _bsp.Textures[patch.ShaderIndex].Name,
				points = vertices.ToArray<Vector3>()
			};
		}

		/// <summary>
		/// Processes a <see cref="Face"/> as a terrain. The vertices of the <see cref="Face"/> are processed into a heightmap
		/// defining the heights of the terrain at that point.
		/// For Quake 3 engine forks only.
		/// </summary>
		/// <param name="face">The <see cref="Face"/> object to process.</param>
		/// <returns>A <see cref="MAPPatch"/> object to be added to a <see cref="MAPBrush"/> object.</returns>
		private MAPTerrainEF2 ProcessEF2Terrain(Face face) {
			string texture = _bsp.Textures[face.TextureIndex].Name;
			int flags = _bsp.Textures[face.TextureIndex].Flags;
			List<Vertex> vertices = _bsp.GetReferencedObjects<Vertex>(face, "Vertices");
			int side = (int)Math.Sqrt(vertices.Count);
			Vector2 mins = new Vector2(float.PositiveInfinity, float.PositiveInfinity);
			Vector2 maxs = new Vector2(float.NegativeInfinity, float.NegativeInfinity);
			Vector3 start = new Vector3(float.NaN, float.NaN, float.NaN);
			foreach (Vertex v in vertices) {
				if (v.position.X < mins.X) {
					mins.X = v.position.X;
				}
				if (v.position.X > maxs.X) {
					maxs.X = v.position.X;
				}
				if (v.position.Y < mins.Y) {
					mins.Y = v.position.Y;
				}
				if (v.position.Y > maxs.Y) {
					maxs.Y = v.position.Y;
				}
				if (v.position.X == mins.X && v.position.Y == mins.Y) {
					start = v.position;
				}
			}
			start.Z = 0;
			float sideLength = maxs.X - mins.X;
			float gridUnit = sideLength / (side - 1);
			float[,] heightMap = new float[side, side];
			float[,] alphaMap = new float[side, side];
			foreach (Vertex v in vertices) {
				int col = (int)Math.Round((v.position.X - mins.X) / gridUnit);
				int row = (int)Math.Round((v.position.Y - mins.Y) / gridUnit);
				heightMap[row, col] = v.position.Z;
			}
			return new MAPTerrainEF2() {
				side = side,
				texture = texture,
				textureShiftS = 0,
				textureShiftT = 0,
				texRot = 0,
				texScaleX = 1,
				texScaleY = 1,
				flags = flags,
				sideLength = sideLength,
				start = start,
				IF = Vector4.Zero,
				LF = Vector4.Zero,
				heightMap = heightMap,
				alphaMap = alphaMap
			};
		}

		/// <summary>
		/// Processes a <see cref="LODTerrain"/> in a <see cref="MAPTerrainMoHAA"/>.
		/// For MoHAA forks only.
		/// </summary>
		/// <param name="lodTerrain">The <see cref="LODTerrain"/> object to process.</param>
		/// <returns>A <see cref="MAPTerrainMoHAA"/> object to be added to a <see cref="MAPBrush"/> object.</returns>
		private MAPTerrainMoHAA ProcessTerrainMoHAA(LODTerrain lodTerrain) {
			string shader = _bsp.Textures[lodTerrain.TextureIndex].Name;
			MAPTerrainMoHAA.Partition partition = new MAPTerrainMoHAA.Partition() {
				shader = shader,
				textureScale = new float[] { 1, 1 },
			};
			MAPTerrainMoHAA terrain = new MAPTerrainMoHAA() {
				size = new Vector2(9, 9),
				flags = ((lodTerrain.Flags & (1 << 6)) > 0) ? 1 : 0,
				origin = new Vector3(lodTerrain.X * 64, lodTerrain.Y * 64, lodTerrain.BaseZ),
			};
			terrain.partitions.Add(partition);
			terrain.partitions.Add(partition);
			terrain.partitions.Add(partition);
			terrain.partitions.Add(partition);
			for (int i = 0; i < 9; ++i) {
				for (int j = 0; j < 9; ++j) {
					MAPTerrainMoHAA.Vertex vertex = new MAPTerrainMoHAA.Vertex() {
						height = lodTerrain.Heightmap[i, j] * 2,
					};
					terrain.vertices.Add(vertex);
				}
			}
			return terrain;
		}

		/// <summary>
		/// Processes a <see cref="Displacement"/> object into a state where it can be output into a file that Hammer can read.
		/// For Source engine forks only.
		/// </summary>
		/// <param name="displacement">The <see cref="Displacement"/> object to process.</param>
		/// <returns>A <see cref="MAPDisplacement"/> object to be added to a <see cref="MAPBrushSide"/>.</returns>
		private MAPDisplacement ProcessDisplacement(Displacement displacement) {
			int power = displacement.Power;
			int first = displacement.FirstVertexIndex;
			Vector3 start = displacement.StartPosition;
			int numVertsInRow = (int)Math.Pow(2, power) + 1;
			Vector3[,] normals = new Vector3[numVertsInRow, numVertsInRow];
			float[,] distances = new float[numVertsInRow, numVertsInRow];
			float[,] alphas = new float[numVertsInRow, numVertsInRow];
			for (int i = 0; i < numVertsInRow; ++i) {
				for (int j = 0; j < numVertsInRow; ++j) {
					normals[i, j] = _bsp.DisplacementVertices[first + (i * numVertsInRow) + j].Normal;
					distances[i, j] = _bsp.DisplacementVertices[first + (i * numVertsInRow) + j].Magnitude;
					alphas[i, j] = _bsp.DisplacementVertices[first + (i * numVertsInRow) + j].Alpha;
				}
			}

			return new MAPDisplacement() {
				power = power,
				start = start,
				normals = normals,
				distances = distances,
				alphas = alphas
			};
		}

		/// <summary>
		/// Changes the progress value of the master object.
		/// </summary>
		private void ReportProgress() {
			int onePercent = _itemsToProcess / 100;
			if (onePercent == 0) {
				onePercent = 1;
			}
			if (_itemsProcessed % onePercent == 0) {
				_master.progress = _itemsProcessed / (double)_itemsToProcess;
			}
		}

		/// <summary>
		/// Looks at the information in the passed <paramref name="face"/> and tries to find the triangle defined
		/// by <paramref name="face"/> with the greatest area. If <paramref name="face"/> does not reference any
		/// vertices then we generate a triangle through the referenced <see cref="Plane"/> instead.
		/// </summary>
		/// <param name="face">The <see cref="Face"/> to find a triangle for.</param>
		/// <returns>Three points defining a triangle which define the plane which <paramref name="face"/> lies on.</returns>
		private Vector3[] GetPointsForFace(Face face, BrushSide brushSide) {
			Vector3[] ret;
			if (face.NumVertices > 2) {
				ret = new Vector3[3];
				float bestArea = 0;
				for (int i = 0; i < face.NumIndices / 3; ++i) {
					Vector3[] temp = new Vector3[] {
						_bsp.Vertices[(int)(face.FirstVertexIndex + _bsp.Indices[face.FirstIndexIndex + (i * 3)])].position,
						_bsp.Vertices[(int)(face.FirstVertexIndex + _bsp.Indices[face.FirstIndexIndex + 1 + (i * 3)])].position,
						_bsp.Vertices[(int)(face.FirstVertexIndex + _bsp.Indices[face.FirstIndexIndex + 2 + (i * 3)])].position
					};
					float area = Vector3Extensions.TriangleAreaSquared(temp[0], temp[1], temp[2]);
					if (area > bestArea) {
						bestArea = area;
						ret = temp;
					}
				}
				if (bestArea > 0.001) {
					return ret;
				}
			}
			if (face.NumEdgeIndices > 0) {
				// TODO: Edges = triangles
			}
			if (face.PlaneIndex >= 0 && face.PlaneIndex < _bsp.Planes.Count) {
				ret = _bsp.Planes[face.PlaneIndex].GenerateThreePoints();
			} else if (brushSide.PlaneIndex >= 0 && brushSide.PlaneIndex < _bsp.Planes.Count) {
				ret = _bsp.Planes[brushSide.PlaneIndex].GenerateThreePoints();
			} else {
				_master.Print("WARNING: Brush side with no points!");
				return new Vector3[3];
			}
			return ret;
		}

	}
}
