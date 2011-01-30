/**
 * Basic model, object and field interfaces that provide three levels of
 * read/write access.
 * 
 * {@link XReadableRepository}, {@link XReadableModel}, {@link XReadableObject}
 * and {@link XReadableField} provide read access: Traverse the
 * repository/model/object/field tree and read revision numbers and field
 * values.
 * 
 * {@link XWritableRepository}, {@link XWritableModel}, {@link XWritableObject}
 * and {@link XWritableField} additionally allow to modify the tree and set
 * field values.
 * 
 * {@link XRevWritableRepository}, {@link XRevWritableModel},
 * {@link XRevWritableObject} and {@link XRevWritableField} provide more lower
 * level access: They allow to explicitly set revision numbers of entities and
 * to add existing entities to a tree.
 */
package org.xydra.base.rmof;

