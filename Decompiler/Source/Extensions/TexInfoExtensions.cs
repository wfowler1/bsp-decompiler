using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Helper class containing methods for working with <see cref="TextureInfo"/> objects.
	/// </summary>
	public static class TexInfoExtensions {

		/// <summary>
		/// Converts a <see cref="TextureInfo"/> object from a BSP file into one that is usable in MAP files. 
		/// Texture axes will be normalized, and scaling will be stored separately.
		/// </summary>
		/// <param name="texInfo">This <see cref="TextureInfo"/>.</param>
		/// <param name="worldPosition">The world coordinates of the entity using this <see cref="TextureInfo"/>. Usually <c>Vector3.zero</c>.</param>
		/// <returns>A <see cref="TextureInfo"/> object for use in MAP output.</returns>
		public static TextureInfo BSP2MAPTexInfo(this TextureInfo texInfo, Vector3 worldPosition) {
			float uScale = 1.0f / texInfo.uAxis.Length();
			float vScale = 1.0f / texInfo.vAxis.Length();
			Vector3 uAxis = Vector3.Normalize(texInfo.uAxis);
			Vector3 vAxis = Vector3.Normalize(texInfo.vAxis);
			float uTranslate = texInfo.translation.X - Vector3.Dot(texInfo.uAxis, worldPosition);
			float vTranslate = texInfo.translation.Y - Vector3.Dot(texInfo.vAxis, worldPosition);
			return new TextureInfo(uAxis, vAxis, new Vector2(uTranslate, vTranslate), new Vector2(uScale, vScale), 0, -1, 0);
		}

		/// <summary>
		/// Validates this <see cref="TextureInfo"/>. This will replace any <c>infinity</c> or <c>NaN</c>
		/// values with valid values to use.
		/// </summary>
		/// <param name="texInfo">The <see cref="TextureInfo"/> to validate.</param>
		/// <param name="plane">The <see cref="Plane"/> of the surface this <see cref="TextureInfo"/> is applied to.</param>
		public static void Validate(this TextureInfo texInfo, Plane plane) {
			// Validate texture scaling
			if (float.IsInfinity(texInfo.scale.X) || float.IsNaN(texInfo.scale.X) || texInfo.scale.X == 0) {
				texInfo.scale = new Vector2(1, texInfo.scale.Y);
			}
			if (float.IsInfinity(texInfo.scale.Y) || float.IsNaN(texInfo.scale.Y) || texInfo.scale.Y == 0) {
				texInfo.scale = new Vector2(texInfo.scale.X, 1);
			}
			// Validate translations
			if (float.IsInfinity(texInfo.translation.X) || float.IsNaN(texInfo.translation.X)) {
				texInfo.translation = new Vector2(0, texInfo.translation.Y);
			}
			if (float.IsInfinity(texInfo.translation.Y) || float.IsNaN(texInfo.translation.Y)) {
				texInfo.translation = new Vector2(texInfo.translation.X, 0);
			}
			// Validate axis components
			if (float.IsInfinity(texInfo.uAxis.X) || float.IsNaN(texInfo.uAxis.X) || float.IsInfinity(texInfo.uAxis.Y) || float.IsNaN(texInfo.uAxis.Y) || float.IsInfinity(texInfo.uAxis.Z) || float.IsNaN(texInfo.uAxis.Z) || texInfo.uAxis == Vector3.Zero) {
				texInfo.uAxis = TextureInfo.TextureAxisFromPlane(plane)[0];
			}
			if (float.IsInfinity(texInfo.vAxis.X) || float.IsNaN(texInfo.vAxis.X) || float.IsInfinity(texInfo.vAxis.Y) || float.IsNaN(texInfo.vAxis.Y) || float.IsInfinity(texInfo.vAxis.Z) || float.IsNaN(texInfo.vAxis.Z) || texInfo.vAxis == Vector3.Zero) {
				texInfo.vAxis = TextureInfo.TextureAxisFromPlane(plane)[1];
			}
			// Validate axes relative to plane ("Texture axis perpendicular to face")
			if (Math.Abs(Vector3.Dot(Vector3.Cross(texInfo.uAxis, texInfo.vAxis), plane.Normal)) < 0.01) {
				Vector3[] newAxes = TextureInfo.TextureAxisFromPlane(plane);
				texInfo.uAxis = newAxes[0];
				texInfo.vAxis = newAxes[1];
			}
		}

	}
}
