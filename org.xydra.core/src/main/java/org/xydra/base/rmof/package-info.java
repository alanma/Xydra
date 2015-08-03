/**
 * Basic model, object and field interfaces that provide three levels of
 * read/write access.
 *
 * {@link org.xydra.base.rmof.XReadableRepository},
 * {@link org.xydra.base.rmof.XReadableModel},
 * {@link org.xydra.base.rmof.XReadableObject} and
 * {@link org.xydra.base.rmof.XReadableField} provide read access: Traverse the
 * repository/model/object/field tree and read revision numbers and field
 * values.
 *
 * {@link org.xydra.base.rmof.XWritableRepository},
 * {@link org.xydra.base.rmof.XWritableModel},
 * {@link org.xydra.base.rmof.XWritableObject} and
 * {@link org.xydra.base.rmof.XWritableField} additionally allow to modify the
 * tree and set field values.
 *
 * {@link org.xydra.base.rmof.XRevWritableRepository},
 * {@link org.xydra.base.rmof.XRevWritableModel},
 * {@link org.xydra.base.rmof.XRevWritableObject} and
 * {@link org.xydra.base.rmof.XRevWritableField} provide more lower level
 * access: They allow to explicitly set revision numbers of entities and to add
 * existing entities to a tree.
 */
package org.xydra.base.rmof;

