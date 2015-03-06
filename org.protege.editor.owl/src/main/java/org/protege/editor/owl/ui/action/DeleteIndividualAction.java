package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.HasObjects;
import org.protege.editor.owl.model.util.OWLObjectRemover;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.view.OWLSelectionViewAction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 20-Apr-2007<br><br>
 */
public class DeleteIndividualAction extends OWLSelectionViewAction {

    private OWLEditorKit owlEditorKit;

    private HasObjects<OWLIndividual> indSetProvider;


    public DeleteIndividualAction(OWLEditorKit owlEditorKit, HasObjects<OWLIndividual> indSetProvider) {
        super("Delete individual(s)", OWLIcons.getIcon("individual.delete.png"));
        this.owlEditorKit = owlEditorKit;
        this.indSetProvider = indSetProvider;
    }


    public void updateState() {
        setEnabled(!indSetProvider.getObjects().isEmpty());
    }


    public void dispose() {
    }


    public void actionPerformed(ActionEvent e) {
        OWLObjectRemover remover = new OWLObjectRemover();
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLIndividual ind : indSetProvider.getObjects()) {
            for (OWLOntology ontology : owlEditorKit.getOWLModelManager().getOntologies()) {
                changes.addAll(remover.getChangesToRemoveObject(ind, ontology));
            }
        }
        owlEditorKit.getModelManager().applyChanges(changes);
    }
}
