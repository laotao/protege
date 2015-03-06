package org.protege.editor.owl.ui.frame.individual;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLIndividualSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

 /**
  * Author: Matthew Horridge<br>
  * The University Of Manchester<br>
  * Bio-Health Informatics Group<br>
  * Date: 29-Jan-2007<br><br>
  */
 public class OWLDifferentIndividualsAxiomFrameSection extends AbstractOWLFrameSection<OWLIndividual, OWLDifferentIndividualsAxiom, Set<OWLIndividual>> {

    public static final String LABEL = "Different Individuals";

    private Set<OWLIndividual> added = new HashSet<OWLIndividual>();


    protected void clear() {
        added.clear();
    }


    public OWLDifferentIndividualsAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLIndividual> frame) {
        super(editorKit, LABEL, "Different individuals", frame);
    }


    /**
     * Refills the section with rows.  This method will be called
     * by the system and should be directly called.
     */
    protected void refill(OWLOntology ontology) {
        for (OWLDifferentIndividualsAxiom ax : ontology.getDifferentIndividualAxioms(getRootObject())) {
            addRow(new OWLDifferentIndividualAxiomFrameSectionRow(getOWLEditorKit(),
                                                                  this,
                                                                  ontology,
                                                                  getRootObject(),
                                                                  ax));
            added.addAll(ax.getIndividuals());
        }
    }


    protected OWLDifferentIndividualsAxiom createAxiom(Set<OWLIndividual> object) {
        object.add(getRootObject());
        return getOWLDataFactory().getOWLDifferentIndividualsAxiom(object);
    }


    public OWLObjectEditor<Set<OWLIndividual>> getObjectEditor() {
        return new OWLIndividualSetEditor(getOWLEditorKit());
    }
    
    @Override
	public boolean checkEditorResults(OWLObjectEditor<Set<OWLIndividual>> editor) {
		Set<OWLIndividual> equivalents = editor.getEditedObject();
		return !equivalents.contains(getRootObject());
	}

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	return change.isAxiomChange() &&
    			change.getAxiom() instanceof OWLDifferentIndividualsAxiom &&
    			((OWLDifferentIndividualsAxiom) change.getAxiom()).getIndividuals().contains(getRootObject());
    }
    
    /**
     * Obtains a comparator which can be used to sort the rows
     * in this section.
     * @return A comparator if to sort the rows in this section,
     *         or <code>null</code> if the rows shouldn't be sorted.
     */
    public Comparator<OWLFrameSectionRow<OWLIndividual, OWLDifferentIndividualsAxiom, Set<OWLIndividual>>> getRowComparator() {
        return null;
    }
}
