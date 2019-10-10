using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Static class containing helper functions for working with <see cref="MAPBrush"/> objects.
	/// </summary>
	public static class MAPBrushExtensions {

		/// <summary>
		/// Moves this <see cref="MAPBrush"/> object in the world by the vector <paramref name="v"/>.
		/// </summary>
		/// <param name="mapBrush">This <see cref="MAPBrush"/>.</param>
		/// <param name="v">Translation vector.</param>
		public static void Translate(this MAPBrush mapBrush, Vector3 v) {
			if (v == Vector3.Zero) {
				return;
			}
			foreach (MAPBrushSide side in mapBrush.sides) {
				side.Translate(v);
				if (side.displacement != null) {
					side.displacement.start += v;
				}
			}
			if (mapBrush.patch != null) {
				mapBrush.patch.Translate(v);
			}
			if (mapBrush.ef2Terrain != null) {
				mapBrush.ef2Terrain.start += v;
			}
		}

		/// <summary>
		/// Creates a <see cref="MAPBrush"/> using <paramref name="froms"/> and <paramref name="tos"/> as a list of edges that create a "winding" in clockwise order.
		/// </summary>
		/// <param name="froms">A list of the "From" vertices. This should match one-to-one with the <paramref name="tos"/> list.</param>
		/// <param name="tos">A list of the "to" vertices. This should match one-to-one with the <paramref name="froms"/> list.</param>
		/// <param name="texture">The texture to use on the front of this brush.</param>
		/// <param name="backtex">The texture to use on the sides and back of this brush.</param>
		/// <param name="texInfo">The texture axis information to be used on the front of this brush.</param>
		/// <param name="xScale">The scale of the texture along the S axis.</param>
		/// <param name="yScale">The scale of the texture along the T axis.</param>
		/// <param name="depth">The desired depth of the brush, how far the back should extend from the front.</param>
		/// <returns>A <see cref="MAPBrush"/> object created using the passed vertices and texture information.</returns>
		public static MAPBrush CreateBrushFromWind(IList<Vector3> froms, IList<Vector3> tos, string texture, string backtex, TextureInfo texInfo, float depth) {
			Vector3[] planepts = new Vector3[3];
			List<MAPBrushSide> sides = new List<MAPBrushSide>(froms.Count + 2); // Each edge, plus a front and back side

			planepts[0] = froms[0];
			planepts[1] = tos[0];
			planepts[2] = tos[1];
			Plane plane = Plane.CreateFromVertices(planepts[0], planepts[2], planepts[1]);
			sides.Add(new MAPBrushSide() {
				vertices = new Vector3[] { planepts[0], planepts[1], planepts[2] },
				plane = plane,
				texture = texture,
				textureInfo = texInfo,
				material = "wld_lightmap",
				lgtScale = 16,
				lgtRot = 0
			});

			Vector3 reverseNormal = -plane.Normal;

			planepts[0] = froms[0] + (reverseNormal * depth);
			planepts[1] = tos[1] + (reverseNormal * depth);
			planepts[2] = tos[0] + (reverseNormal * depth);
			Plane backplane = Plane.CreateFromVertices(planepts[0], planepts[2], planepts[1]);
			Vector3[] generatedAxes = TextureInfo.TextureAxisFromPlane(backplane);
			sides.Add(new MAPBrushSide() {
				vertices = new Vector3[] { planepts[0], planepts[1], planepts[2] },
				plane = backplane,
				texture = backtex,
				textureInfo = new TextureInfo(generatedAxes[0], generatedAxes[1], Vector2.Zero, Vector2.One, 0, 0, 0),
				material = "wld_lightmap",
				lgtScale = 16,
				lgtRot = 0
			});

			// For each edge
			for (int i = 0; i < froms.Count; ++i) {
				planepts[0] = froms[i];
				planepts[1] = froms[i] + (reverseNormal * depth);
				planepts[2] = tos[i];
				Plane sideplane = Plane.CreateFromVertices(planepts[0], planepts[2], planepts[1]);
				generatedAxes = TextureInfo.TextureAxisFromPlane(sideplane);
				sides.Add(new MAPBrushSide() {
					vertices = new Vector3[] { planepts[0], planepts[1], planepts[2] },
					plane = sideplane,
					texture = backtex,
					textureInfo = new TextureInfo(generatedAxes[0], generatedAxes[1], Vector2.Zero, Vector2.One, 0, 0, 0),
					material = "wld_lightmap",
					lgtScale = 16,
					lgtRot = 0
				});
			}

			return new MAPBrush() {
				sides = sides
			};
		}

		/// <summary>
		/// Creates an axis-aligned cubic brush with bounds from <paramref name="mins"/> to <paramref name="maxs"/>.
		/// </summary>
		/// <param name="mins">The minimum extents of the new brush.</param>
		/// <param name="maxs">The maximum extents of the new brush.</param>
		/// <param name="texture">The texture to use on this brush.</param>
		/// <returns>The resulting <see cref="MAPBrush"/> object.</returns>
		public static MAPBrush CreateCube(Vector3 mins, Vector3 maxs, string texture) {
			MAPBrush newBrush = new MAPBrush();
			Vector3[][] planes = new Vector3[6][];
			for (int i = 0; i < 6; ++i) {
				planes[i] = new Vector3[3];
			} // Six planes for a cube brush, three vertices for each plane
			float[][] textureS = new float[6][];
			for (int i = 0; i < 6; ++i) {
				textureS[i] = new float[3];
			}
			float[][] textureT = new float[6][];
			for (int i = 0; i < 6; ++i) {
				textureT[i] = new float[3];
			}
			// The planes and their texture scales
			// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
			// Top
			planes[0][0] = new Vector3(mins.X, maxs.Y, maxs.Z);
			planes[0][1] = new Vector3(maxs.X, maxs.Y, maxs.Z);
			planes[0][2] = new Vector3(maxs.X, mins.Y, maxs.Z);
			textureS[0][0] = 1;
			textureT[0][1] = -1;
			// Bottom
			planes[1][0] = new Vector3(mins.X, mins.Y, mins.Z);
			planes[1][1] = new Vector3(maxs.X, mins.Y, mins.Z);
			planes[1][2] = new Vector3(maxs.X, maxs.Y, mins.Z);
			textureS[1][0] = 1;
			textureT[1][1] = -1;
			// Left
			planes[2][0] = new Vector3(mins.X, maxs.Y, maxs.Z);
			planes[2][1] = new Vector3(mins.X, mins.Y, maxs.Z);
			planes[2][2] = new Vector3(mins.X, mins.Y, mins.Z);
			textureS[2][1] = 1;
			textureT[2][2] = -1;
			// Right
			planes[3][0] = new Vector3(maxs.X, maxs.Y, mins.Z);
			planes[3][1] = new Vector3(maxs.X, mins.Y, mins.Z);
			planes[3][2] = new Vector3(maxs.X, mins.Y, maxs.Z);
			textureS[3][1] = 1;
			textureT[3][2] = -1;
			// Near
			planes[4][0] = new Vector3(maxs.X, maxs.Y, maxs.Z);
			planes[4][1] = new Vector3(mins.X, maxs.Y, maxs.Z);
			planes[4][2] = new Vector3(mins.X, maxs.Y, mins.Z);
			textureS[4][0] = 1;
			textureT[4][2] = -1;
			// Far
			planes[5][0] = new Vector3(maxs.X, mins.Y, mins.Z);
			planes[5][1] = new Vector3(mins.X, mins.Y, mins.Z);
			planes[5][2] = new Vector3(mins.X, mins.Y, maxs.Z);
			textureS[5][0] = 1;
			textureT[5][2] = -1;

			for (int i = 0; i < 6; i++) {
				MAPBrushSide currentSide = new MAPBrushSide() {
					vertices = planes[i],
					plane = Plane.CreateFromVertices(planes[i][0], planes[i][2], planes[i][1]),
					texture = texture,
					textureInfo = new TextureInfo(new Vector3(textureS[i][0], textureS[i][1], textureS[i][2]), new Vector3(textureT[i][0], textureT[i][1], textureT[i][2]), Vector2.Zero, Vector2.One, 0, 0, 0),
					material = "wld_lightmap",
					lgtScale = 16,
					lgtRot = 0
				};
				newBrush.sides.Add(currentSide);
			}
			return newBrush;
		}

	}
}
