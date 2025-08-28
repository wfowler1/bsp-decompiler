using LibBSP;

namespace Decompiler
{
    /// <summary>
    /// Static class containing helper functions for operating with <see cref="Brush"/> objects.
    /// </summary>
    public static class BrushExtensions
    {
        // Contents flags:
        // Quake: https://github.com/id-Software/Quake/blob/master/WinQuake/bspfile.h#L137
        // Quake 2: https://github.com/id-Software/Quake-2/blob/372afde46e7defc9dd2d719a1732b8ace1fa096e/qcommon/qfiles.h#L333
        // Quake 3: https://github.com/id-Software/Quake-III-Arena/blob/dbe4ddb10315479fc00086f08e25d968b4b43c49/code/game/surfaceflags.h
        // Source: https://github.com/ValveSoftware/source-sdk-2013/blob/68c8b82fdcb41b8ad5abde9fe1f0654254217b8e/src/public/bspflags.h

        /// <summary>
        /// Determines if the contents of the passed <see cref="Brush"/> have the "detail" flag set.
        /// </summary>
        /// <param name="brush">This <see cref="Brush"/>.</param>
        /// <param name="bsp">The <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
        /// <returns><c>true</c> if the contents indicate detail, <c>false</c> otherwise.</returns>
        public static bool IsDetail(this Brush brush, BSP bsp)
        {
            if (bsp.MapType == MapType.Nightfire)
            {
                return ((brush.Contents & (1 << 9)) != 0);
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake3))
            {
                int texture = brush.TextureIndex;
                if (texture >= 0)
                {
                    return ((bsp.Textures[texture].Contents & (1 << 27)) != 0);
                }
                return false;
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake2)
                || bsp.MapType.IsSubtypeOf(MapType.Source))
            {
                return ((brush.Contents & (1 << 27)) != 0);
            }

            return false;
        }

        /// <summary>
        /// Determines if the contents of the passed <see cref="Brush"/> have the "water" flag set.
        /// </summary>
        /// <param name="brush">This <see cref="Brush"/>.</param>
        /// <param name="bsp">The <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
        /// <returns><c>true</c> if the contents indicate water, <c>false</c> otherwise.</returns>
        public static bool IsWater(this Brush brush, BSP bsp)
        {
            if (bsp.MapType.IsSubtypeOf(MapType.Quake))
            {
                return brush.Contents == -3;
            }
            else if (bsp.MapType == MapType.Nightfire)
            {
                return ((brush.Contents & (1 << 20)) != 0);
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake3))
            {
                int texture = brush.TextureIndex;
                if (texture >= 0)
                {
                    return ((bsp.Textures[texture].Contents & (1 << 5)) != 0);
                }
                return false;
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake2)
                || bsp.MapType.IsSubtypeOf(MapType.Source))
            {
                return ((brush.Contents & (1 << 5)) != 0);
            }

            return false;
        }

        /// <summary>
        /// Determines if the contents of the passed <see cref="Brush"/> have the "lava" flag set.
        /// </summary>
        /// <param name="brush">This <see cref="Brush"/>.</param>
        /// <param name="bsp">The <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
        /// <returns><c>true</c> if the contents indicate lava, <c>false</c> otherwise.</returns>
        public static bool IsLava(this Brush brush, BSP bsp)
        {
            if (bsp.MapType.IsSubtypeOf(MapType.Quake))
            {
                return brush.Contents == -5;
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake3))
            {
                int texture = brush.TextureIndex;
                if (texture >= 0)
                {
                    return ((bsp.Textures[texture].Contents & (1 << 3)) != 0);
                }
                return false;
            }
            else if (bsp.MapType.IsSubtypeOf(MapType.Quake2))
            {
                return ((brush.Contents & (1 << 3)) != 0);
            }

            return false;
        }

        /// <summary>
        /// Determines if the surface flags of the passed <see cref="Brush"/> specify a manual vis brush.
        /// </summary>
        /// <param name="brush">This <see cref="Brush"/>.</param>
        /// <param name="version">The type of <see cref="BSP"/> the <paramref name="brush"/> is from.</param>
        /// <returns><c>true</c> if the surface flags indicate manual vis, <c>false</c> otherwise.</returns>
        public static bool IsManVis(this Brush brush, BSP bsp)
        {
            if (bsp.MapType.IsSubtypeOf(MapType.MOHAA))
            {
                int texture = brush.TextureIndex;
                if (texture >= 0)
                {
                    return bsp.Textures[texture].Flags == 0x40010990;
                }
            }

            return false;
        }

    }
}
