using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Static class containing helper functions for operating with <see cref="Brush"/> objects.
	/// </summary>
	public static class BrushExtensions {

		/// <summary>
		/// Determines if the <see cref="Brush.contents"/> of the passed <see cref="Brush"/> have the "detail" flag set.
		/// </summary>
		/// <param name="brush">This <see cref="Brush"/>.</param>
		/// <param name="bsp">The <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
		/// <returns><c>true</c> if the <see cref="Brush.contents"/> indicate detail, <c>false</c> otherwise.</returns>
		public static bool IsDetail(this Brush brush, BSP bsp) {
			if (bsp.MapType == MapType.Nightfire) {
				return ((brush.Contents & (1 << 9)) != 0);
			} else if (bsp.MapType.IsSubtypeOf(MapType.Quake3)) {
				int texture = brush.TextureIndex;
				if (texture >= 0) {
					return ((bsp.Textures[texture].Contents & (1 << 27)) != 0);
				}
				return false;
			} else if (bsp.MapType.IsSubtypeOf(MapType.Quake2)
				|| bsp.MapType.IsSubtypeOf(MapType.Source)) {
				return ((brush.Contents & (1 << 27)) != 0);
			}

			return false;
		}

		/// <summary>
		/// Determines if the <see cref="Brush.contents"/> of the passed <see cref="Brush"/> have the "water" flag set.
		/// </summary>
		/// <param name="brush">This <see cref="Brush"/>.</param>
		/// <param name="version">The type of <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
		/// <returns><c>true</c> if the <see cref="Brush.contents"/> indicate water, <c>false</c> otherwise.</returns>
		public static bool IsWater(this Brush brush, BSP bsp) {
			if (bsp.MapType.IsSubtypeOf(MapType.Quake)) {
				return brush.Contents == -3;
			} else if (bsp.MapType == MapType.Nightfire) {
				return ((brush.Contents & (1 << 20)) != 0);
			} else if (bsp.MapType.IsSubtypeOf(MapType.Quake3)) {
				int texture = brush.TextureIndex;
				if (texture >= 0) {
					return ((bsp.Textures[texture].Contents & (1 << 5)) != 0);
				}
				return false;
			} else if (bsp.MapType.IsSubtypeOf(MapType.Quake2)
				|| bsp.MapType.IsSubtypeOf(MapType.Source)) {
				return ((brush.Contents & (1 << 5)) != 0);
			}

			return false;
		}

		/// <summary>
		/// Determines if the surface flags of the passed <see cref="Brush"/> specify a manual vis brush.
		/// </summary>
		/// <param name="brush">This <see cref="Brush"/>.</param>
		/// <param name="version">The type of <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
		/// <returns><c>true</c> if the surface flags indicate manual vis, <c>false</c> otherwise.</returns>
		public static bool IsManVis(this Brush brush, BSP bsp) {
			if (bsp.MapType.IsSubtypeOf(MapType.MOHAA)) {
				int texture = brush.TextureIndex;
				if (texture >= 0) {
					return bsp.Textures[texture].Flags == 0x40010990;
				}
			}

			return false;
		}

	}
}
