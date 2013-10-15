/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2011, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.tutorials.featureeditor;

import java.math.BigDecimal;

import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.internal.commands.edit.SetEditFeatureCommand;
import net.refractions.udig.project.internal.commands.edit.WriteEditFeatureCommand;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tutorials.celleditor.FormComboBoxCellEditor;
import net.refractions.udig.tutorials.celleditor.FormTextCellEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

/**
 * A remake of the original 'Edit Feature' tutorial example with the ff changes:
 * <p>
 * <ul>
 * <li>use Eclipse Forms</li>
 * <li>use the natural earth "countries" dataset</li>
 * <li>use CellEditors for the form's fields</li>
 * </ul>
 * </p>
 * 
 * @author Naz Chan
 */
public class CountryPanelForm
        implements
            KeyListener,
            ISelectionChangedListener,
            FocusListener,
            MouseListener {

    /** Attribute name for attribute NAME_FORMA */
    public final static String NAME_FORMA = "NAME_FORMA";
    public final static String NAME_FORMA_LBL = "Name (formal)";

    /** Attribute name for attribute NAME_SORT */
    public final static String NAME_SORT = "NAME_SORT";
    public final static String NAME_SORT_LBL = "Name (short)";

    /** Attribute name for attribute POP_EST */
    public final static String POP_EST = "POP_EST";
    public final static String POP_EST_LBL = "Population Estimate";

    /** Attribute name for attribute TYPE */
    public final static String TYPE = "TYPE";
    public final static String TYPE_SOV_LBL = "Sovereign Country";
    public final static String TYPE_COU_LBL = "Country";

    /** Attribute name for attribute MAP_COLOR */
    public final static String COLOR_MAP = "MAP_COLOR";
    public final static String COLOR_MAP_LBL = "Map Color";
    public final static Double[] COLOR_MAP_OPTS;
    static {
        COLOR_MAP_OPTS = new Double[13];
        for( int i = 0; i < COLOR_MAP_OPTS.length; i++ ) {
            COLOR_MAP_OPTS[i] = Double.valueOf(Integer.toString(i + 1));
        }
    }

    /**
     * Used to construct UI
     */
    private static final int MAX_COLS = 2;
    private static final int MIN_COLS = 1;
    private static final int LABEL_STYLE = SWT.SHADOW_IN;
    private static final int FIELD_STYLE = SWT.SHADOW_IN | SWT.BORDER;
    private static final int SECTION_STYLE = Section.TWISTIE | Section.TITLE_BAR
            | Section.DESCRIPTION | Section.EXPANDED;

    private FormToolkit toolkit;
    private ScrolledForm form;

    private Text nameSort;
    private Text nameFormal;
    private Text population;
    private Button type;
    private ComboViewer colorMap;

    private Action apply;
    private Action reset;

    /**
     * Used for editing the feature
     */
    private SimpleFeature editedFeature;
    private SimpleFeature oldFeature;

    /**
     * Used send commands to the edit blackboard
     */
    private IToolContext context;

    public void createControl( Composite parent, FormToolkit toolkit ) {
        
        this.toolkit = toolkit;

        form = toolkit.createScrolledForm(parent);
        form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter(){
            @Override
            public void linkActivated( HyperlinkEvent e ) {
                final IMessage[] errors = (IMessage[]) e.data;
                if (errors.length > 0) {
                    final IMessage topError = errors[0];
                    topError.getControl().setFocus();
                }
            }
        });
        form.getMessageManager().setMessagePrefixProvider(new IMessagePrefixProvider(){
            @Override
            public String getPrefix( Control control ) {
                return control.getData().toString() + " - ";
            }
        });
        form.setText("Country Form");
        final ColumnLayout layout = new ColumnLayout();
        layout.maxNumColumns = MAX_COLS;
        layout.minNumColumns = MIN_COLS;
        form.getBody().setLayout(layout);
        toolkit.decorateFormHeading(form.getForm());

        final Section section = toolkit.createSection(form.getBody(), SECTION_STYLE);
        section.setText("Country Details");
        section.setDescription("Update country details below.");
        section.addExpansionListener(new ExpansionAdapter(){
            public void expansionStateChanged( ExpansionEvent e ) {
                // Nothing
            }
        });
        final Composite client = toolkit.createComposite(section, SWT.NONE);
        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 4;
        client.setLayout(sectionLayout);
        section.setClient(client);

        // SWT Widgets
        Label label = toolkit.createLabel(client, NAME_FORMA_LBL, LABEL_STYLE);
        nameFormal = toolkit.createText(client, "", FIELD_STYLE);
        nameFormal.setData(NAME_FORMA_LBL);
        nameFormal.addFocusListener(this);
        nameFormal.addKeyListener(this);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        nameFormal.setLayoutData(layoutData);

        label = toolkit.createLabel(client, NAME_SORT_LBL, LABEL_STYLE);
        nameSort = toolkit.createText(client, "", FIELD_STYLE);
        nameSort.setData(NAME_SORT_LBL);
        nameSort.addFocusListener(this);
        nameSort.addKeyListener(this);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        nameSort.setLayoutData(layoutData);

        label = toolkit.createLabel(client, POP_EST_LBL, LABEL_STYLE);
        population = toolkit.createText(client, "", FIELD_STYLE);
        population.setData(POP_EST_LBL);
        population.addFocusListener(this);
        population.addKeyListener(this);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        population.setLayoutData(layoutData);

        label = toolkit.createLabel(client, "", LABEL_STYLE);
        type = toolkit.createButton(client, TYPE_SOV_LBL, SWT.CHECK);
        type.addMouseListener(this);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        type.setLayoutData(layoutData);
        
        // JFace Viewer
        label = toolkit.createLabel(client, "Color Map:", LABEL_STYLE);
        colorMap = new ComboViewer(client, FIELD_STYLE);
        colorMap.addSelectionChangedListener(this);
        layoutData = new GridData();
        layoutData.horizontalSpan = 3;
        colorMap.getControl().setLayoutData(layoutData);

        // hook up to data
        colorMap.setContentProvider(new IStructuredContentProvider(){
            public Object[] getElements( Object inputElement ) {
                if (inputElement instanceof Object[]) {
                    return (Object[]) inputElement;
                }
                return null;
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
                // for dynamic content we would register listeners here
            }
            public void dispose() {
            }
        });
        colorMap.setLabelProvider(new LabelProvider(){
            public String getText( Object element ) {
                return " " + element + " color";
            }
        });
        colorMap.setInput(COLOR_MAP_OPTS);

        // Other sample section - to try out ColumnLayout
        final Section sectionOther = toolkit.createSection(form.getBody(), SECTION_STYLE);
        sectionOther.setText("Others");
        sectionOther
                .setDescription("Sample section to demo ColumnLayout, make the view width smaller to force it to relayout.");
        sectionOther.addExpansionListener(new ExpansionAdapter(){
            public void expansionStateChanged( ExpansionEvent e ) {
                // Nothing
            }
        });
        final Composite clientOther = toolkit.createComposite(sectionOther, SWT.NONE);
        sectionLayout = new GridLayout();
        sectionLayout.numColumns = 4;
        clientOther.setLayout(sectionLayout);
        sectionOther.setClient(clientOther);

        Label remarksLbl = toolkit.createLabel(clientOther, "Remarks:", LABEL_STYLE);
        Text remarks = toolkit.createText(clientOther, "", FIELD_STYLE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        remarks.setLayoutData(layoutData);

        // Buttons
        apply = new Action("Apply"){
            @Override
            public void run() {
                applyChanges();
            }
        };
        apply.setEnabled(false);
        form.getToolBarManager().add(apply);

        reset = new Action("Reset"){
            @Override
            public void run() {
                resetChanges();
            }
        };
        reset.setEnabled(false);
        form.getToolBarManager().add(reset);

        form.getToolBarManager().update(true);

    }

    public void setFocus() {
        nameFormal.setFocus();
    }

    private void setEnabled( boolean enabled ) {
        if (oldFeature == null && enabled) {
            return;
        }
        apply.setEnabled(enabled);
        reset.setEnabled(enabled);
    }

    @Override
    public void selectionChanged( SelectionChangedEvent event ) {
        setEnabled(true);
    }

    @Override
    public void keyPressed( KeyEvent e ) {
        // Nothing
    }

    @Override
    public void keyReleased( KeyEvent e ) {
        setEnabled(true);
    }

    @Override
    public void focusGained( FocusEvent e ) {
        // Nothing
    }

    @Override
    public void focusLost( FocusEvent e ) {
        final Object control = e.getSource();
        if (control == population) {
            isValid(population);
            isNumber(population);
        } else {
            isValid((Text) control);
        }
    }

    @Override
    public void mouseDoubleClick( MouseEvent e ) {
        // Nothing
    }

    @Override
    public void mouseDown( MouseEvent e ) {
        // Nothing
    }

    @Override
    public void mouseUp( MouseEvent e ) {
        setEnabled(true);
    }

    public void setEditFeature( SimpleFeature newFeature, IToolContext newcontext ) {
        this.context = newcontext;
        oldFeature = newFeature;

        if (oldFeature != null) {
            try {
                editedFeature = SimpleFeatureBuilder.copy(newFeature);
            } catch (IllegalAttributeException e) {
                // shouldn't happen
            }
        } else {
            editedFeature = null;
        }
        if (oldFeature == null) {
            nameSort.setText("");
            colorMap.setSelection(new StructuredSelection());
            nameFormal.setText("");
        } else {

            // Set UI value for NAME_FORMA
            String nameFormalVal = (String) oldFeature.getAttribute(NAME_FORMA);
            if (nameFormalVal == null) nameFormalVal = "";
            nameFormal.setText(nameFormalVal);

            // Set UI value for NAME_SORT
            String nameSortStr = (String) oldFeature.getAttribute(NAME_SORT);
            if (nameSortStr == null) nameSortStr = "";
            nameSort.setText(nameSortStr);

            // Set UI value for POP_EST
            BigDecimal popEst = new BigDecimal((Double) oldFeature.getAttribute(POP_EST));
            population.setText(popEst.toString());

            // Set UI value for TYPE
            String typeStr = (String) oldFeature.getAttribute(TYPE);
            type.setSelection(TYPE_SOV_LBL.equalsIgnoreCase(typeStr));

            // Set UI value for COLOR_MAP
            Double colorText = (Double) oldFeature.getAttribute(COLOR_MAP);
            if (colorText != null) {
                colorMap.setSelection(new StructuredSelection(colorText));
            } else {
                colorMap.setSelection(new StructuredSelection());
            }

        }
        setEnabled(false);
    }

    private void resetChanges() {
        setEditFeature(oldFeature, context);
        setEnabled(false);
        form.getMessageManager().removeAllMessages();
    }

    private void applyChanges() {

        if (!verifyChanges()) {
            try {

                // Set feature value for NAME_FORMA
                editedFeature.setAttribute(NAME_FORMA, nameFormal.getText());

                // Set feature value for NAME_SORT
                editedFeature.setAttribute(NAME_SORT, nameSort.getText());

                // Set feature value for POP_EST
                editedFeature.setAttribute(POP_EST, Double.valueOf(population.getText()));

                // Set feature value for TYPE
                editedFeature.setAttribute(TYPE, type.getSelection() ? TYPE_SOV_LBL : TYPE_COU_LBL);

                // Set feature value for COLOR_MAP
                StructuredSelection colorSelection = (StructuredSelection) colorMap.getSelection();
                editedFeature.setAttribute(COLOR_MAP, colorSelection.getFirstElement());

            } catch (IllegalAttributeException e1) {
                // shouldn't happen.
            }

            final CompositeCommand compComm = new CompositeCommand();
            //Sets the feature (with the edited values) used in the view as the editFeature
            compComm.getCommands().add(new SetEditFeatureCommand(editedFeature));
            //Write the changes to the actual dataset
            compComm.getCommands().add(new WriteEditFeatureCommand());
            context.sendASyncCommand(compComm);

            setEnabled(false);
        }

    }

    private boolean verifyChanges() {
        boolean hasError = false;
        if (!isValid(nameFormal)) {
            hasError = true;
        }
        if (!isValid(nameSort)) {
            hasError = true;
        }
        if (!(isValid(population) && isNumber(population))) {
            hasError = true;
        }
        return hasError;
    }

    private boolean isValid( Text text ) {
        if (editedFeature != null) {
            form.getMessageManager().removeMessages(text);
            if (text.getText() == null || "".equals(text.getText())) {
                form.getMessageManager().addMessage(text, "Must not be blank.", null,
                        IMessage.ERROR, text);
                return false;
            }
        }
        return true;
    }

    private boolean isNumber( Text text ) {
        if (editedFeature != null) {
            try {
                Double.valueOf(text.getText());
            } catch (NumberFormatException e) {
                form.getMessageManager().addMessage(text, "Must be a number.", null,
                        IMessage.ERROR, text);
                return false;
            }
        }
        return true;
    }

}
