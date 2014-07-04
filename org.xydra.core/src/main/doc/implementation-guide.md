# Xydra Implementation Guide

## Comparing XCommand and XEvent 

<table border="1">

<tr>
<th>Property</th>
<th>XCommand</th>
<th>XEvent</th>
</tr>

<tr>
<td>repositoryId</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>modelId</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>objectId</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td> **revision number** </td>
<td>yes: used as command mode, FORCED, SAFE (state-bound) or SAFE (revision-bound, when rev > 0)</td>
<td>yes: used as resulting revision number</td>
</tr>

<tr>
<td> **forced** </td>
<td>yes, TODO unify with revNr</td>
<td>--</td>
</tr>

<tr>
<td>repositoryId</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>changedEntity</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>target</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>changeType</td>
<td>yes</td>
<td>yes</td>
</tr>

<tr>
<td>actorId</td>
<td>--</td>
<td>yes</td>
</tr>

<tr>
<td>oldFieldRev</td>
<td>--</td>
<td>yes</td>
</tr>

<tr>
<td>oldObjectRev</td>
<td>--</td>
<td>yes</td>
</tr>

<tr>
<td>oldModelRev</td>
<td>--</td>
<td>yes</td>
</tr>

<tr>
<td>bool: inTransaction</td>
<td>--</td>
<td>yes</td>
</tr>

<tr>
<td>bool: isImplied</td>
<td>--</td>
<td>yes</td>
</tr>

</table>