package org.protege.editor.owl.ui.editor;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.selector.OWLIndividualSelectorPanel;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.swing.*;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 09-Feb-2007<br>
 * <br>
 */
public class OWLIndividualEditor extends AbstractOWLObjectEditor<OWLIndividual> {

    private OWLIndividualSelectorPanel selectorPanel;

	public OWLIndividualEditor(OWLEditorKit owlEditorKit) {
		this.selectorPanel = new OWLIndividualSelectorPanel(owlEditorKit);
	}

	/**
	 * Builds an OWLIndividualEditor instance with the input selection mode The
	 * legal values are the same of the ListSelectionModel constants and the
	 * default value is ListSelectionModel.SINGLE_SELECTION
	 * 
	 * @param owlEditorKit
	 * @param selectionMode
	 */
	public OWLIndividualEditor(OWLEditorKit owlEditorKit, int selectionMode) {
		selectorPanel = new OWLIndividualSelectorPanel(owlEditorKit, selectionMode);
	}


    public String getEditorTypeName() {
        return "Named Individual";
    }


    public boolean canEdit(Object object) {
        return object instanceof OWLIndividual;
    }


    /**
	 * Gets a component that will be used to edit the specified object.
	 * 
	 * @return The component that will be used to edit the object
	 */
	public JComponent getEditorComponent() {
		return selectorPanel;
	}


	public OWLIndividual getEditedObject() {
		return selectorPanel.getSelectedObject();
	}


    public Set<OWLIndividual> getEditedObjects() {
		return selectorPanel.getSelectedObjects();
	}


    public boolean setEditedObject(OWLIndividual editedObject) {
        selectorPanel.setSelection(editedObject);
        return true;
    }


    public void dispose() {
		selectorPanel.dispose();
	}
}
