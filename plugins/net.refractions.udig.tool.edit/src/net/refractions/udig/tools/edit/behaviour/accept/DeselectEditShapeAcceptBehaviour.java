/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.tools.edit.behaviour.accept;

import java.util.List;

import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.DeselectEditGeomCommand;
import net.refractions.udig.tools.edit.commands.SetEditStateCommand;
import net.refractions.udig.tools.edit.preferences.PreferenceConstants;
import net.refractions.udig.tools.edit.support.EditGeom;

/**
 * If the {@link PreferenceConstants#P_SELECT_POST_ACCEPT} preference is true then this behaviour will
 * deselect the newly created feature.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class DeselectEditShapeAcceptBehaviour implements Behaviour {

    public UndoableMapCommand getCommand( EditToolHandler handler ) {
        UndoableComposite composite = new UndoableComposite();
        List<EditGeom> geoms = handler.getEditBlackboard(handler.getEditLayer()).getGeoms();
        composite.getCommands().add(new DeselectEditGeomCommand(handler, geoms ));
        composite.getCommands().add(new SetEditStateCommand(handler, EditState.NONE));
        return composite;
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log(error.getMessage(), error);
    }

    public boolean isValid( EditToolHandler handler ) {
        // might seem odd but the WriteChangesBehaviour leaves the created feature selected. 
        return !EditPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SELECT_POST_ACCEPT);
    }

}
