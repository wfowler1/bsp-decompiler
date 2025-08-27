using System;
using System.Collections.Generic;
using System.Globalization;

using LibBSP;

namespace Decompiler
{
    /// <summary>
    /// Helper class containing methods for working with <see cref="StaticProp"/> objects.
    /// </summary>
    public static class StaticPropExtensions
    {

        private static IFormatProvider _format = CultureInfo.CreateSpecificCulture("en-US");

        /// <summary>
        /// Parse the properties of this <see cref="StaticProp"/> into an <see cref="Entity"/> object.
        /// </summary>
        /// <param name="prop">This <see cref="StaticProp"/>.</param>
        /// <param name="dictionary">The model names dictionary from the Static Props lump.</param>
        /// <returns><see cref="Entity"/> representation of this <see cref="StaticProp"/>.</returns>
        public static Entity ToEntity(this StaticProp prop, IList<string> dictionary)
        {
            Entity entity = new Entity("prop_static");
            entity["model"] = dictionary[prop.ModelIndex];
            entity["skin"] = prop.Skin.ToString();
            entity.Origin = prop.Origin;
            entity.Angles = prop.Angles;
            entity["solid"] = prop.Solidity.ToString();
            entity["fademindist"] = prop.MinimumFadeDistance.ToString(_format);
            entity["fademaxdist"] = prop.MaximumFadeDistance.ToString(_format);
            entity["fadescale"] = prop.ForcedFadeScale.ToString(_format);
            if (prop.Name != null)
            {
                entity["targetname"] = prop.Name;
            }
            return entity;
        }

    }
}
