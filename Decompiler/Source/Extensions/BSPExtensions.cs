using System;
using System.Collections.Generic;
using System.Linq;

using LibBSP;

namespace Decompiler {
	/// <summary>
	/// Static class containing helper functions for operating on <see cref="BSP"/> objects.
	/// </summary>
	public static class BSPExtensions {

		/// <summary>
		/// Gets all the <see cref="Face"/> objects associated with the given <paramref name="model"/>.
		/// </summary>
		/// <param name="bsp">This <see cref="BSP"/> object.</param>
		/// <param name="model">The <see cref="Model"/> object to get all <see cref="Face"/> objects for.</param>
		/// <returns>
		/// A <see cref="List"/>&lt;<see cref="Face"/>&gt; containing all the <see cref="Face"/> objects occurring
		/// within the passed <see cref="Model"/> object, or <c>null</c> if the BSP doesn't have faces.
		/// </returns>
		public static List<Face> GetFacesInModel(this BSP bsp, Model model) {
			if (model.FirstFaceIndex >= 0) {
				return bsp.GetReferencedObjects<Face>(model, "Faces");
			}

			return new List<Face>(0);
		}

		/// <summary>
		/// Gets all the <see cref="Brush"/> objects associated with the given <paramref name="model"/>.
		/// </summary>
		/// <param name="bsp">This <see cref="BSP"/> object.</param>
		/// <param name="model">The <see cref="Model"/> object to get all <see cref="Brush"/> objects for.</param>
		/// <returns>
		/// A <see cref="List"/>&lt;<see cref="Brush"/>&gt; containing all the <see cref="Brush"/> objects occurring
		/// within the passed <see cref="Model"/> object, or <c>null</c> if the BSP doesn't have brushes.
		/// </returns>
		/// <remarks>
		/// The proper way to go from a <see cref="Model"/> reference to a collection of <see cref="Brush"/> objects
		/// depends on the type of BSP we're operating on. In the case of Quake 3, for example, models directly reference
		/// brushes, but Quake 2 and Source require a full tree traversal.
		/// </remarks>
		public static List<Brush> GetBrushesInModel(this BSP bsp, Model model) {
			if (model.FirstBrushIndex >= 0) {
				return bsp.GetReferencedObjects<Brush>(model, "Brushes");
			}

			if (model.FirstLeafIndex >= 0) {
				List<Leaf> leavesInModel = bsp.GetReferencedObjects<Leaf>(model, "Leaves");
				return bsp.GetBrushesInLeafList(leavesInModel);
			}

			if (model.HeadNodeIndex >= 0) {
				return bsp.GetBrushesInLeafList(bsp.GetLeavesInTree(bsp.Nodes[model.HeadNodeIndex]));
			}

			return new List<Brush>(0);
		}

		/// <summary>
		/// Gets all <see cref="Brush"/> objects referenced from a list of <see cref="Leaf"/> objects.
		/// </summary>
		/// <param name="bsp">This <see cref="BSP"/>.</param>
		/// <param name="leaves">A <see cref="List"/> of <see cref="Leaf"/> objects to get <see cref="Brush"/> references from.</param>
		/// <returns>All the <see cref="Brush"/> objects referenced from the given <see cref="Leaf"/> objects.</returns>
		public static List<Brush> GetBrushesInLeafList(this BSP bsp, IEnumerable<Leaf> leaves) {
			List<Brush> brushes = new List<Brush>();
			bool[] brushesUsed = new bool[bsp.Brushes.Count];
			foreach (Leaf leaf in leaves) {
				List<long> markBrushesInLeaf = bsp.GetReferencedObjects<long>(leaf, "LeafBrushes");
				foreach (long markBrush in markBrushesInLeaf) {
					if (!brushesUsed[(int)markBrush]) {
						brushes.Add(bsp.Brushes[(int)markBrush]);
						brushesUsed[(int)markBrush] = true;
					}
				}
			}
			return brushes;
		}

		/// <summary>
		/// Gets all the leaves referenced from the passed <see cref="Node"/> object.
		/// </summary>
		/// <remarks>
		/// Since nodes reference other nodes, this may recurse quite a lot. Any given node will
		/// reference a set of leaves. This is an iterative preorder traversal algorithm modified
		/// from the Wikipedia page at: http://en.wikipedia.org/wiki/Tree_traversal on April 19, 2012.
		/// The cited example has since been removed but can still be found at
		/// http://en.wikipedia.org/w/index.php?title=Tree_traversal&amp;oldid=488219889#Iterative_Traversal
		/// A recursive algorithm would overflow the stack, so a <c>Stack</c> object in memory is
		/// used instead.
		/// </remarks>
		/// <param name="bsp">This <see cref="BSP"/> object.</param>
		/// <param name="node">The <see cref="Node"/> object at the head of this tree.</param>
		/// <returns>A <c>List&lt;<see cref="Leaf"/>&gt;</c> from the tree starting from <paramref name="node"/>.</returns>
		public static List<Leaf> GetLeavesInTree(this BSP bsp, Node node) {
			List<Leaf> leaves = new List<Leaf>();

			Stack<Node> nodestack = new Stack<Node>();
			nodestack.Push(node);
			Node currentNode;

			while (!(nodestack.Count == 0)) {
				currentNode = nodestack.Pop();
				int right = currentNode.Child2Index;
				if (right >= 0) {
					nodestack.Push(bsp.Nodes[right]);
				} else {
					leaves.Add(bsp.Leaves[(right * -1) - 1]);
				}
				int left = currentNode.Child1Index;
				if (left >= 0) {
					nodestack.Push(bsp.Nodes[left]);
				} else {
					leaves.Add(bsp.Leaves[(left * -1) - 1]);
				}
			}

			return leaves;
		}

	}
}
