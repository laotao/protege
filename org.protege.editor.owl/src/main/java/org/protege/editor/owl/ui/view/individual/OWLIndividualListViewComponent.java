package org.protege.editor.owl.ui.view.individual;

import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.util.HasObjects;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.action.DeleteIndividualAction;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.CreateNewTarget;
import org.protege.editor.owl.ui.view.Deleteable;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityCollector;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br>
 * <br>
 * <p/> This definitely needs a rethink - it is a totally inefficient hack!
 */

public class OWLIndividualListViewComponent extends AbstractOWLIndividualViewComponent
        implements Findable<OWLNamedIndividual>, Deleteable, CreateNewTarget, RefreshableComponent {

    /**
     * 
     */
    private static final long serialVersionUID = -1519269944342726754L;
    private OWLObjectList<OWLIndividual> list;
    private OWLOntologyChangeListener listener;
    private ChangeListenerMediator changeListenerMediator;
    private OWLModelManagerListener modelManagerListener;
    private int selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
    private boolean selectionChangedByUser = true;

    protected Set<OWLIndividual> individualsInList;

    private ListSelectionListener listSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                if (list.getSelectedValue() != null && selectionChangedByUser) {
                    setGlobalSelection(list.getSelectedValue());
                }
                changeListenerMediator.fireStateChanged(OWLIndividualListViewComponent.this);
            }
        }
    };


    public void initialiseIndividualsView() throws Exception {
        list = new OWLObjectList<>(getOWLEditorKit());
        list.setSelectionMode(selectionMode);
        setLayout(new BorderLayout());
        add(new JScrollPane(list));
        list.addListSelectionListener(listSelectionListener);
        list.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                setGlobalSelection(list.getSelectedValue());
            }
        });
        listener = new OWLOntologyChangeListener() {
            public void ontologiesChanged(
                    List<? extends OWLOntologyChange> changes) {
                processChanges(changes);
            }
        };
        getOWLModelManager().addOntologyChangeListener(listener);

        setupActions();
        changeListenerMediator = new ChangeListenerMediator();
        individualsInList = new TreeSet<>(getOWLModelManager().getOWLObjectComparator());
        refill();
        modelManagerListener = new OWLModelManagerListener() {
            public void handleChange(OWLModelManagerChangeEvent event) {
                if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED)) {
                    refill();
                }
            }
        };
        getOWLModelManager().addListener(modelManagerListener);
    }


    protected void setupActions() {
        addAction(new AddIndividualAction(), "A", "A");
        addAction(new DeleteIndividualAction(getOWLEditorKit(),
                                             new HasObjects<OWLIndividual>() {
                                                 public Set<OWLIndividual> getObjects() {
                                                     return getSelectedIndividuals();
                                                 }
                                             }), "B", "A");
    }


    public void refreshComponent() {
        refill();
    }


    protected void refill() {
        // Initial fill
        individualsInList.clear();
        for (OWLOntology ont : getOntologies()) {
            individualsInList.addAll(ont.getIndividualsInSignature());
            individualsInList.addAll(ont.getReferencedAnonymousIndividuals());
        }
        reset();
    }


    protected Set<OWLOntology> getOntologies() {
        return getOWLModelManager().getActiveOntologies();
    }


    public void setSelectedIndividual(OWLIndividual individual) {
        list.setSelectedValue(individual, true);
    }


    protected void reset() {
        OWLIndividual [] objects = individualsInList.toArray(new OWLIndividual[individualsInList.size()]);
        list.setListData(objects);
        OWLNamedIndividual individual = getSelectedOWLIndividual();
        selectionChangedByUser = false;
        try {
            list.setSelectedValue(individual, true);
        }
        finally {
            selectionChangedByUser = true;
        }
    }

    public OWLIndividual updateView(OWLIndividual selelectedIndividual) {
        if (!isPinned()) {
            list.setSelectedValue(selelectedIndividual, true);
        }
        return list.getSelectedValue();
    }

    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(listener);
        getOWLModelManager().removeListener(modelManagerListener);
    }

    public OWLIndividual getSelectedIndividual() {
        return list.getSelectedValue();
    }

    public Set<OWLIndividual> getSelectedIndividuals() {
        return new LinkedHashSet<>(list.getSelectedValuesList());
    }

    protected void processChanges(List<? extends OWLOntologyChange> changes) {
    	Set<OWLEntity> possiblyAddedObjects = new HashSet<>();
    	Set<OWLEntity> possiblyRemovedObjects = new HashSet<>();
        OWLEntityCollector addedCollector = new OWLEntityCollector(possiblyAddedObjects);
        OWLEntityCollector removedCollector = new OWLEntityCollector(possiblyRemovedObjects);
        for (OWLOntologyChange chg : changes) {
            if (chg.isAxiomChange()) {
                OWLAxiomChange axChg = (OWLAxiomChange) chg;
                if (axChg instanceof AddAxiom) {
                    axChg.getAxiom().accept(addedCollector);
                } else {
                    axChg.getAxiom().accept(removedCollector);
                }
            }
        }
        boolean mod = false;
        for (OWLEntity ent : possiblyAddedObjects) {
            if (ent instanceof OWLIndividual) {
                if (individualsInList.add((OWLNamedIndividual) ent)) {
                    mod = true;
                }
            }
        }
        for (OWLEntity ent : possiblyRemovedObjects) {
            if (ent instanceof OWLIndividual) {
                boolean stillReferenced = false;
                for (OWLOntology ont : getOntologies()) {
                    if (ont.containsIndividualInSignature(ent.getIRI())) {
                        stillReferenced = true;
                        break;
                    }
                }
                if (!stillReferenced) {
                    if (individualsInList.remove((OWLNamedIndividual) ent)) {
                        mod = true;
                    }
                }
            }
        }
        if (mod) {
            reset();
        }
    }

    protected void addIndividual() {
        OWLEntityCreationSet<OWLNamedIndividual> set = getOWLWorkspace().createOWLIndividual();
        if (set == null) {
            return;
        }
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.addAll(set.getOntologyChanges());
        changes.addAll(dofurtherCreateSteps(set.getOWLEntity()));
        getOWLModelManager().applyChanges(changes);
        OWLNamedIndividual ind = set.getOWLEntity();
        if (ind != null) {
            list.setSelectedValue(ind, true);
        }
    }


    protected List<OWLOntologyChange> dofurtherCreateSteps(OWLIndividual newIndividual) {
        return Collections.emptyList();
    }


    public List<OWLNamedIndividual> find(String match) {
        return new ArrayList<>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLIndividuals(match));
    }

    public void show(OWLNamedIndividual owlEntity) {
        list.setSelectedValue(owlEntity, true);
    }


    public void setSelectedIndividuals(Set<OWLIndividual> individuals) {
        list.setSelectedValues(individuals, true);
    }


    private class AddIndividualAction extends DisposableAction {
        /**
         * 
         */
        private static final long serialVersionUID = 4574601252717263757L;

        public AddIndividualAction() {
            super("Add individual", OWLIcons.getIcon("individual.add.png"));
        }

        public void actionPerformed(ActionEvent e) {
            addIndividual();
        }

        public void dispose() {
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeListenerMediator.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListenerMediator.removeChangeListener(listener);
    }

    public void handleDelete() {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLIndividual ind : getSelectedIndividuals()) {
            for(OWLOntology ont : getOWLModelManager().getOntologies()) {
                if(ind.isNamed()) {
                    OWLNamedIndividual namedIndividual = ind.asOWLNamedIndividual();
                    for(OWLAxiom ax : ont.getReferencingAxioms(namedIndividual)) {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                    for(OWLAnnotationAssertionAxiom ax : ont.getAnnotationAssertionAxioms(namedIndividual.getIRI())) {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                }
                else {
                    OWLAnonymousIndividual anonymousIndividual = ind.asOWLAnonymousIndividual();
                    for(OWLAxiom ax : ont.getReferencingAxioms(anonymousIndividual)) {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                    for(OWLAnnotationAssertionAxiom ax : ont.getAnnotationAssertionAxioms(anonymousIndividual)) {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                }

                for(OWLAnnotationAssertionAxiom ax : ont.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
                    if(ax.getValue().equals(ind)) {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                }
            }
        }
        getOWLModelManager().applyChanges(changes);
    }

    public boolean canDelete() {
        return !getSelectedIndividuals().isEmpty();
    }

    public boolean canCreateNew() {
        return true;
    }

    public void createNewObject() {
        addIndividual();
    }

    public void setSelectionMode(int selectionMode) {
        if (list != null) {
            list.setSelectionMode(selectionMode);
        }
    }
    
    public void setIndividualListColor(Color c) {
        list.setBackground(c);
    }
}
