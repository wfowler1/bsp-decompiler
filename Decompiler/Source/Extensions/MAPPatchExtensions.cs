using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

using LibBSP;

namespace Decompiler
{
    /// <summary>
    /// Static class containing helper functions for working with <see cref="MAPPatch"/> objects.
    /// </summary>
    public static class MAPPatchExtensions
    {

        /// <summary>
        /// Moves this <see cref="MAPPatch"/> object using the passed vector <paramref name="v"/>.
        /// </summary>
        /// <param name="mapBrushSide">This <see cref="MAPPatch"/>.</param>
        /// <param name="v">Translation vector.</param>
        public static void Translate(this MAPPatch mapPatch, Vector3 v)
        {
            for (int i = 0; i < mapPatch.points.Length; ++i)
            {
                mapPatch.points[i] += v;
            }
        }

    }
}
