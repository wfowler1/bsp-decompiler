using System.Numerics;
using System.Text;

using LibBSP;

namespace Decompiler
{

    /// <summary>
    /// Static class containing helper methods for working with <see cref="Overlay"/> objects.
    /// </summary>
    public static class OverlayExtensions
    {

        /// <summary>
        /// Parse the properties of this <see cref="Overlay"/> into an <see cref="Entity"/> object.
        /// </summary>
        /// <param name="overlay">This <see cref="Overlay"/>.</param>
        /// <param name="sideIndices">The <see cref="MAPBrushSide"/> ids for the faces this <see cref="Overlay"/> is applied to.</param>
        /// <param name="name">Name of this <see cref="Entity"/> (for triggers).</param>
        /// <returns><see cref="Entity"/> representation of this <see cref="Overlay"/>.</returns>
        public static Entity ToEntity(this Overlay overlay, int[] sideIndices, string name = null)
        {
            BSP bsp = overlay.Parent.Bsp;
            TextureInfo textureInfo = overlay.TextureInfo;
            TextureData textureData = bsp.TextureData[textureInfo.TextureIndex];

            Entity entity = new Entity("info_overlay");
            entity.Origin = overlay.Origin;
            entity["BasisOrigin"] = overlay.Origin.X + " " + overlay.Origin.Y + " " + overlay.Origin.Z;
            entity["BasisNormal"] = overlay.BasisNormal.X + " " + overlay.BasisNormal.Y + " " + overlay.BasisNormal.Z;
            entity["StartU"] = overlay.U.X.ToString();
            entity["EndU"] = overlay.U.Y.ToString();
            entity["StartV"] = overlay.V.X.ToString();
            entity["EndV"] = overlay.V.Y.ToString();
            entity["uv0"] = overlay.UVPoint0.X + " " + overlay.UVPoint0.Y + " 0";
            entity["uv1"] = overlay.UVPoint1.X + " " + overlay.UVPoint1.Y + " 0";
            entity["uv2"] = overlay.UVPoint2.X + " " + overlay.UVPoint2.Y + " 0";
            entity["uv3"] = overlay.UVPoint3.X + " " + overlay.UVPoint3.Y + " 0";
            entity["material"] = bsp.Textures.GetTextureAtOffset(textureData.TextureStringOffset);
            entity["RenderOrder"] = overlay.RenderOrder.ToString();
            entity["fademindist"] = "-1";
            entity["fademaxdist"] = "0";

            Vector3 uBasis = new Vector3(overlay.UVPoint0.Z, overlay.UVPoint1.Z, overlay.UVPoint2.Z);
            bool negateV = overlay.UVPoint3.Z == 1;
            entity["BasisU"] = uBasis.X + " " + uBasis.Y + " " + uBasis.Z;
            Vector3 vBasis = overlay.BasisNormal.Cross(uBasis).GetNormalized();
            if (negateV)
            {
                vBasis = -vBasis;
            }
            entity["BasisV"] = vBasis.X + " " + vBasis.Y + " " + vBasis.Z;

            StringBuilder sides = new StringBuilder();
            for (int i = 0; i < sideIndices.Length; ++i)
            {
                if (sideIndices[i] >= 0)
                {
                    sides.Append(sideIndices[i]).Append(' ');
                }
            }
            entity["sides"] = sides.ToString().TrimEnd(' ');

            if (!string.IsNullOrWhiteSpace(name))
            {
                entity.Name = name;
            }

            return entity;
        }

    }
}
