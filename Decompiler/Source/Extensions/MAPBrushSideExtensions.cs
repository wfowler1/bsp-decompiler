using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Static class containing helper functions for working with <see cref="MAPBrushSide"/> objects.
	/// </summary>
	public static class MAPBrushSideExtensions {

		/// <summary>
		/// Moves this <see cref="MAPBrushSide"/> object using the passed vector <paramref name="v"/>.
		/// </summary>
		/// <param name="mapBrushSide">This <see cref="MAPBrushSide"/>.</param>
		/// <param name="v">Translation vector.</param>
		public static void Translate(this MAPBrushSide mapBrushSide, Vector3 v) {
			for (int i = 0; i < mapBrushSide.vertices.Length; ++i) {
				mapBrushSide.vertices[i] += v;
			}
			mapBrushSide.plane = Plane.CreateFromVertices(mapBrushSide.vertices[0], mapBrushSide.vertices[2], mapBrushSide.vertices[1]);
		}

	}
}
