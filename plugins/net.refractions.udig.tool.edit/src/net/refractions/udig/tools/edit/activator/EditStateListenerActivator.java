/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.tools.edit.activator;

import net.refractions.udig.project.BlackboardEvent;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IBlackboardListener;
import net.refractions.udig.project.ui.tool.ModalTool;
import net.refractions.udig.project.ui.tool.Tool;
import net.refractions.udig.project.ui.tool.ToolConstants;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;

/**
 * Adds and removes a listener to the handler that sets the icons when the EditState Changes.
 * 
 * @author jones
 * @since 1.1.0
 */
public class EditStateListenerActivator implements Activator {

    private IconManager iconManager;
    
    public void activate( EditToolHandler handler ) {
    	iconManager = new IconManager(handler);
    	handler.getContext().getMap().getBlackboard().addListener(iconManager);
    }
    
    public void deactivate( EditToolHandler handler ) {
        handler.getContext().getMap().getBlackboard().removeListener(iconManager);
    	iconManager = null;
    }

    public void handleActivateError( EditToolHandler handler, Throwable error ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

    public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

    class IconManager implements IBlackboardListener {

        private EditToolHandler handler;

        IconManager(EditToolHandler handler2){
            this.handler=handler2;
        }
        
        public void blackBoardChanged( BlackboardEvent event ) {
            if (iconManager == null) {
                event.getSource().removeListener(this);
                return;
            }

            if (event.getKey() == EditToolHandler.EDITSTATE) {
                EditState oldState=(EditState) event.getOldValue();
                EditState newState=(EditState) event.getNewValue();
                if( newState==null )
                    newState=EditState.NONE;
                if (oldState == newState)
                    return;

                switch( newState ) {
                case NONE:
                case MODIFYING:
                case CREATING:
                	//FIXME
                	Tool tool = handler.getTool();
                	String defaultCursorId = (String)tool.getProperty(ToolConstants.DEFAULT_CURSOR_ID_KEY);
                    handler.setCursor(defaultCursorId);
                    break;
                case ILLEGAL:
                    handler.setCursor(ModalTool.NO_CURSOR);
                    break;
                case MOVING:
                    handler.setCursor(ModalTool.MOVE_CURSOR);
                    break;
                case BUSY:
                case COMMITTING:
                    handler.setCursor(ModalTool.WAIT_CURSOR);
                    break;
                }
            }
        }

        public void blackBoardCleared( IBlackboard source ) {
        }
    }

}
