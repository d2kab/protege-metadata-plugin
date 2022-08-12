package edu.stanford.bmir.protege.examples.view;


import com.google.common.base.Optional;
import edu.stanford.bmir.protege.examples.Main;
import edu.stanford.bmir.protege.examples.models.MODProperty;
import edu.stanford.bmir.protege.examples.models.OntologyMetadataContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.util.AugmentedJTextField;
import org.protege.editor.core.ui.util.LinkLabel;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.refactor.ontology.EntityIRIUpdaterOntologyChangeStrategy;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;


@SuppressWarnings("Guava")
public class OWLOntologyMetadataView extends AbstractOWLViewComponent {


    public static final String ONTOLOGY_IRI_FIELD_LABEL = "Ontology IRI";
    public static final String ONTOLOGY_VERSION_IRI_FIELD_LABEL = "Ontology Version IRI";
    public static final URI ONTOLOGY_IRI_DOCUMENTATION = URI.create("https://www.w3.org/TR/owl2-syntax/#Ontology_IRI_and_Version_IRI");
    public static final URI VERSION_IRI_DOCUMENTATION = URI.create("https://www.w3.org/TR/owl2-syntax/#Versioning_of_OWL_2_Ontologies");
    private static final long serialVersionUID = 1252038674995535772L;
    private final AugmentedJTextField ontologyIRIField = new AugmentedJTextField("e.g http://www.example.com/ontologies/myontology");

    // private OWLOntologyAnnotationList list;
    private final AugmentedJTextField ontologyVersionIRIField = new AugmentedJTextField("e.g. http://www.example.com/ontologies/myontology/1.0.0");
    //private final OWLOntologyChangeListener ontologyChangeListener = owlOntologyChanges -> myHandleOntologyChanges(owlOntologyChanges);
    private OWLModelManagerListener listener;
    private boolean updatingViewFromModel = false;
    private boolean updatingModelFromView = false;
    /**
     * The IRI of the ontology when the ontology IRI field gets the focus.
     */


    private OWLOntologyID initialOntologyID = null;
    private boolean ontologyIRIShowing = false;

    OntologyMetadataContainer root = new OntologyMetadataContainer();

    BundleContext context = FrameworkUtil.getBundle(OWLOntologyMetadataView.class).getBundleContext();
    URL url = context.getBundle().getResource("mod-v2.0_profile.ttl"); // returns  bundle://44.0:1/mod-v2.0_profile.ttl

    Set<MODProperty> properties = root.getModProperties(url.openConnection().getInputStream());

    public OWLOntologyMetadataView() throws OWLOntologyCreationException, IOException {
    }

    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    OWLOntology o = m.loadOntologyFromOntologyDocument(url.openConnection().getInputStream());

    protected void initialiseOWLView() throws Exception {





        HashMap<MODProperty, List<String>> modProperties = mapModToActiveOntology(properties, activeOntology());
        showActiveOntologyModProperties(modProperties);
  /*
        listener = event -> {
            try {
                myHandleModelManagerChangeEvent(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
      getOWLModelManager().addListener(listener);

        getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);

        myUpdateView();
        */
    }

    private HashMap<MODProperty, List<String>> mapModToActiveOntology(Set<MODProperty> properties, OWLOntology ontology) throws IOException, OWLOntologyCreationException {
        HashMap<MODProperty, List<String>> out = new HashMap<>();
        Collection<OWLAnnotation> annotations = ontology.getAnnotations();
        for (MODProperty prop : properties) {
            Collection<OWLNamedIndividual> equivalentProperties = prop.getEquivalentProperty();
            if (ontology != null) {
             if (!out.containsKey(prop)) {
                    out.put(prop, new ArrayList<String>());
                }
                for (OWLAnnotation annotation : annotations) {
                    String value = null;
                    if (annotation.getProperty().getIRI().toURI().toString().equals(prop.getIRI().toString())) {
                        OWLLiteral literal = annotation.getValue().asLiteral().orNull();
                        IRI iri = annotation.getValue().asIRI().orNull();
                        value = "";
                        if (literal != null) {
                            value = literal.getLiteral();
                        } else if (iri != null) {
                            value = iri.toString();
                        }
                    }
                     if (value != "" && value != null) {
                        out.get(prop).add(value);
                    }
                }
            }
        }
        return out;
    }


    private Set<MODProperty> usedProperties(Set<MODProperty> properties , OWLOntology ontology){
            Set<MODProperty> out = new HashSet<>();
            Collection<OWLAnnotation> annotations = ontology.getAnnotations();
            for (MODProperty prop : properties) {
                Collection<OWLNamedIndividual> equivalentProperties = prop.getEquivalentProperty();
                MODProperty usedProperty = null;
                if (ontology != null) {
                    for (OWLAnnotation annotation : annotations) {
                        for (OWLNamedIndividual indiv : equivalentProperties) {
                            if (annotation.getProperty().getIRI().toURI().toString().equals(indiv.getIRI().toString())) {
                                usedProperty = new MODProperty(indiv, o);
                                out.add(usedProperty);
                            }
                        }
                    }
                }
        }
            return out;
    }
private Map<String , String> prefixesMap() throws IOException, OWLOntologyCreationException {
    URL url = Main.class.getClassLoader().getResource("mod-v2.0_profile.ttl");
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntology o = manager.loadOntologyFromOntologyDocument(url.openConnection().getInputStream());
    OWLDocumentFormat format = manager.getOntologyFormat(o);
    Map<String,String> map = new HashMap<>();
    if (format.isPrefixOWLOntologyFormat()) {
        map = format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap();
    }
    return map;
}
    private void addAnnotation(JTextArea component,MODProperty prop) {
        String value = component.getText();
        OWLDataFactory df = activeOntology().getOWLOntologyManager().getOWLDataFactory();
        OWLAnnotationProperty annotProp=df.getOWLAnnotationProperty(prop.getIRI());
        OWLAnnotation annot = df.getOWLAnnotation(annotProp,df.getOWLLiteral(value));
        AddOntologyAnnotation add = new AddOntologyAnnotation(activeOntology(),annot);
        activeOntology().getOWLOntologyManager().applyChange(add);
        try {
            activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
        } catch (OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        }
    }

    private void showActiveOntologyModProperties(HashMap<MODProperty, List<String>> modProperties) throws OWLOntologyCreationException, IOException {
        showHeader();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1, 25, 25));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        for (Map.Entry<MODProperty, List<String>> prop : modProperties.entrySet()) {
            JPanel panel = showModPropertyCard(prop.getKey(), prop.getValue());
            mainPanel.add(panel);
        }
        JScrollPane scroll = new JScrollPane(mainPanel, VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scroll);
    }

    private JPanel showModPropertyCard(MODProperty prop, List<String> values) throws IOException, OWLOntologyCreationException {
        Set<MODProperty> usedProperties  = usedProperties(properties,activeOntology());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.white);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel northPanel = new JPanel(new GridLayout());
        northPanel.setBackground(Color.white);
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.white);
        centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel southPanel = new JPanel(new GridLayout());
        southPanel.setBackground(Color.white);
        southPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JButton save = new JButton("save values");
        save.setPreferredSize(new Dimension(40,40));
        JButton addMetadata = new JButton("Add Value");
        addMetadata.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                centerPanel.add(new JTextArea(" "));
            }
        });
        centerPanel.add(addMetadata,BorderLayout.WEST);
        if (values.size() == 0) {
            JTextArea area = new JTextArea();
            centerPanel.add(area);
        }
        for (String value : values) {
            JTextArea area = new JTextArea(value);
            centerPanel.add(area);
        }
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (Component component : centerPanel.getComponents()){
                 //RemoveOntologyAnnotation rm = new RemoveOntologyAnnotation(activeOntology())
                    if (component instanceof JTextArea) {
                        for (OWLAnnotation annotation : activeOntology().getAnnotations()) {
                            OWLLiteral literal = annotation.getValue().asLiteral().orNull();
                            IRI iri = annotation.getValue().asIRI().orNull();
                            String value = "";
                            if (literal != null) {
                                value = literal.getLiteral();
                            } else if (iri != null) {
                                value = iri.toString();
                            }
                            if (annotation.getProperty().getIRI().equals(prop.getIRI())) {
                                Set<String> set = new HashSet<String>();
                                for (Component comp : centerPanel.getComponents()) {
                                    if (comp instanceof JTextArea) {
                                        set.add(((JTextArea) comp).getText());
                                    }
                                }
                                if (!set.contains(value)) {
                                    RemoveOntologyAnnotation rm = new RemoveOntologyAnnotation(activeOntology(), annotation);
                                    activeOntology().getOWLOntologyManager().applyChange(rm);
                                    try {
                                        activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
                                    } catch (OWLOntologyStorageException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            }
                            addAnnotation((JTextArea) component, prop);
                        }
                        }
                    }
                    }



        });
        centerPanel.setPreferredSize(new Dimension(100,100));
        JLabel label = new JLabel();
        for (String propLabel : prop.getLabel()) {
            if (propLabel.endsWith("@en")) {
                label = new JLabel("<html>" + propLabel.substring(1, propLabel.length() - 4) + "</html>");
            }
        }
        label.setBackground(Color.white);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Chandas", 1, 17));
        JLabel description = new JLabel("<html>" + prop.getDescription().substring(1, prop.getDescription().length() - 4) + "</html>");
        description.setFont(new Font("TeXGyreCursor", Font.PLAIN, 15));
        description.setPreferredSize(new Dimension(150, 250));
        northPanel.add(label);
        northPanel.add(description);
        JLabel equivalentProperties = new JLabel("Equivalent Properties");
        equivalentProperties.setFont(new Font("TeXGyreChorus", 2, 17));
        equivalentProperties.setPreferredSize(new Dimension(70,100));
        southPanel.add(equivalentProperties);
        Map<String,String > prefixes = prefixesMap();
        for (OWLNamedIndividual indiv : prop.getEquivalentProperty()) {
            Set<String> set = prefixes.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), indiv.getIRI().getNamespace())).map(Map.Entry::getKey).collect(Collectors.toSet());
            JRadioButton button = null;
            Iterator it = set.iterator();
            if (it.hasNext()){
                button = new JRadioButton(it.next()+indiv.toString());
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        OWLDataFactory df = activeOntology().getOWLOntologyManager().getOWLDataFactory();
                        OWLAnnotationProperty annotProp=df.getOWLAnnotationProperty(indiv.getIRI());
                        for (String value : values) {
                            OWLAnnotation annot = df.getOWLAnnotation(annotProp,df.getOWLLiteral(value));
                            AddOntologyAnnotation add = new AddOntologyAnnotation(activeOntology(), annot);
                            activeOntology().getOWLOntologyManager().applyChange(add);
                        }
                        try {
                            activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
                        } catch (OWLOntologyStorageException e) {
                            throw new RuntimeException(e);
                        }
                        for (OWLAnnotation annotation : activeOntology().getAnnotations()) {
                            if (annotation.getProperty().getIRI().equals(prop.getIRI())) {
                                RemoveOntologyAnnotation rm = new RemoveOntologyAnnotation(activeOntology(), annotation);
                                activeOntology().getOWLOntologyManager().applyChange(rm);
                            }
                            try {
                                activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
                            } catch (OWLOntologyStorageException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        for (Component component : centerPanel.getComponents()){
                            if (component instanceof  JRadioButton){
                                if (!((JRadioButton) component).getText().equals(it.next()+indiv.toString())){
                                    ((JRadioButton) component).setSelected(false);
                                }
                            }
                        }
                    }
                });
            }
            else{
                button = new JRadioButton(indiv.toString());
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        OWLDataFactory df = activeOntology().getOWLOntologyManager().getOWLDataFactory();
                        OWLAnnotationProperty annotProp=df.getOWLAnnotationProperty(indiv.getIRI());
                        for (String value : values) {
                            OWLAnnotation annot = df.getOWLAnnotation(annotProp,df.getOWLLiteral(value));
                            AddOntologyAnnotation add = new AddOntologyAnnotation(activeOntology(), annot);
                            activeOntology().getOWLOntologyManager().applyChange(add);
                        }
                        try {
                            activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
                        } catch (OWLOntologyStorageException e) {
                            throw new RuntimeException(e);
                        }
                        for (OWLAnnotation annotation : activeOntology().getAnnotations()) {
                            if (annotation.getProperty().getIRI().equals(prop.getIRI())) {
                                RemoveOntologyAnnotation rm = new RemoveOntologyAnnotation(activeOntology(), annotation);
                                activeOntology().getOWLOntologyManager().applyChange(rm);
                            }
                            try {
                                activeOntology().getOWLOntologyManager().saveOntology(activeOntology());
                            } catch (OWLOntologyStorageException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        for (Component component : centerPanel.getComponents()){
                            if (component instanceof  JRadioButton){
                                if (!((JRadioButton) component).getText().equals(indiv.toString())){
                                    ((JRadioButton) component).setSelected(false);
                                }
                            }
                        }
                    }
                });
            }
            if (usedProperties.contains(new MODProperty(indiv,o))){
                button.setSelected(true);
            }
            button.setBackground(Color.white);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            southPanel.add(button);
        }
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        JScrollPane centerScroll = new JScrollPane(centerPanel, VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centerScroll.setPreferredSize(new Dimension(100, 150));
        mainPanel.add(centerScroll);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        mainPanel.add(save,BorderLayout.EAST);
        return mainPanel;
    }



    private void showHeader() {
        setLayout(new BorderLayout());
        JPanel ontologyIRIPanel = new JPanel(new GridBagLayout());
        add(ontologyIRIPanel, BorderLayout.NORTH);
        Insets insets = new Insets(0, 4, 2, 0);
        ontologyIRIPanel.add(new LinkLabel(ONTOLOGY_IRI_FIELD_LABEL, e -> {
            showOntologyIRIDocumentation();
        }), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0, 0));
        ontologyIRIPanel.add(ontologyIRIField, new GridBagConstraints(1, 0, 1, 1, 100.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        ontologyIRIField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateModelFromView();
            }

            public void removeUpdate(DocumentEvent e) {
                updateModelFromView();
            }

            public void changedUpdate(DocumentEvent e) {

            }
        });
        ontologyIRIField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                handleOntologyIRIFieldFocusLost();
            }

            @Override
            public void focusGained(FocusEvent e) {
                handleOntologyIRIFieldFocusGained();
            }
        });
        ontologyIRIShowing = ontologyIRIField.isShowing();
        ontologyIRIField.addHierarchyListener(e -> {
            handleComponentHierarchyChanged();
        });

        ontologyIRIPanel.add(new LinkLabel(ONTOLOGY_VERSION_IRI_FIELD_LABEL, e -> {
            showVersionIRIDocumentation();
        }), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, insets, 0, 0));

        ontologyIRIPanel.add(ontologyVersionIRIField, new GridBagConstraints(1, 1, 1, 1, 100.0, 0.0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, insets, 0, 0));

        ontologyVersionIRIField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateModelFromView();
            }

            public void removeUpdate(DocumentEvent e) {
                updateModelFromView();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });


        ontologyIRIPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    }


    private void handleComponentHierarchyChanged() {
        if (ontologyIRIShowing != ontologyIRIField.isShowing()) {
            ontologyIRIShowing = ontologyIRIField.isShowing();
            if (!ontologyIRIField.isShowing()) {
                handlePossibleOntologyIdUpdate();
            } else {
                handleOntologyIRIFieldActivated();
            }
        }
    }

    private void handleOntologyIRIFieldFocusGained() {
        handleOntologyIRIFieldActivated();
    }

    private void handleOntologyIRIFieldActivated() {
        initialOntologyID = getOWLModelManager().getActiveOntology().getOntologyID();
    }

    private void handleOntologyIRIFieldFocusLost() {
        handlePossibleOntologyIdUpdate();
    }

    private void handlePossibleOntologyIdUpdate() {
        OWLOntologyID id = createOWLOntologyIDFromView();
        if (isOntologyIRIChange(id)) {
            EntityIRIUpdaterOntologyChangeStrategy changeStrategy = new EntityIRIUpdaterOntologyChangeStrategy();
            Set<OWLEntity> entities = changeStrategy.getEntitiesToRename(activeOntology(), initialOntologyID, id);
            if (!entities.isEmpty()) {
                boolean rename = showConfirmRenameDialog(id, entities);
                if (rename) {
                    List<OWLOntologyChange> changes = changeStrategy.getChangesForRename(activeOntology(), initialOntologyID, id);
                    getOWLModelManager().applyChanges(changes);
                    initialOntologyID = id;
                }
            }


        }
    }

    private boolean showConfirmRenameDialog(OWLOntologyID id, Set<OWLEntity> entities) {
        String msg = getChangeEntityIRIsConfirmationMessage(id, entities);
        int ret = JOptionPane.showConfirmDialog(this, msg, "Rename entities as well as ontology?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return ret == JOptionPane.YES_OPTION;
    }

    private boolean isOntologyIRIChange(OWLOntologyID id) {
        return initialOntologyID != null && id != null && !id.equals(initialOntologyID) && !initialOntologyID.isAnonymous() && !id.isAnonymous();
    }

    private String getChangeEntityIRIsConfirmationMessage(OWLOntologyID id, Set<OWLEntity> entities) {
        return "<html><body>You have renamed the ontology from<br>" + "" + initialOntologyID.getOntologyIRI().get().toString() + "<br>" + "to<br>" + "" + id.getOntologyIRI().get().toString() + ".<br>" + "<br>" + "<b>There are " + NumberFormat.getIntegerInstance().format(entities.size()) + " entities whose IRIs start with the original ontology IRI. Would you also like to rename these entities<br>" + "so that their IRIs start with the new ontology IRI?</b></body></html>";
    }


   /* private void myHandleModelManagerChangeEvent(OWLModelManagerChangeEvent event) throws IOException {
        if (isUpdateTriggeringEvent(event)) {
            myUpdateView();
        }
    }*/

    private boolean isUpdateTriggeringEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_LOADED) || event.isType(EventType.ONTOLOGY_RELOADED) || event.isType(EventType.ONTOLOGY_SAVED);
    }

    private void showVersionIRIDocumentation() {
        try {
            Desktop.getDesktop().browse(VERSION_IRI_DOCUMENTATION);
        } catch (IOException ex) {
            ErrorLogPanel.showErrorDialog(ex);
        }
    }

    private void showOntologyIRIDocumentation() {
        try {
            Desktop.getDesktop().browse(ONTOLOGY_IRI_DOCUMENTATION);
        } catch (IOException ex) {
            ErrorLogPanel.showErrorDialog(ex);
        }
    }

    /**
     * Updates the view from the model - unless the changes were triggered by changes in the view.
     */


    private void updateViewFromModel() {
        if (updatingModelFromView) {
            return;
        }
        updatingViewFromModel = true;
        try {
            OWLOntology activeOntology = getOWLEditorKit().getOWLModelManager().getActiveOntology();
            if (activeOntology.isAnonymous()) {
                if (!ontologyIRIField.getText().isEmpty()) {
                    ontologyIRIField.setText("");
                    if (ontologyVersionIRIField.getText().isEmpty()) {
                        ontologyVersionIRIField.setText("");
                    }
                }
            } else {
                OWLOntologyID id = activeOntology.getOntologyID();

                Optional<IRI> ontologyIRI = id.getOntologyIRI();
                String ontologyIRIString = ontologyIRI.get().toString();
                if (ontologyIRI.isPresent()) {
                    if (!ontologyIRIField.getText().equals(ontologyIRIString)) {
                        ontologyIRIField.setText(ontologyIRIString);
                    }
                }

                Optional<IRI> versionIRI = id.getVersionIRI();
                if (versionIRI.isPresent()) {
                    String versionIRIString = versionIRI.get().toString();
                    if (!ontologyVersionIRIField.getText().equals(versionIRIString)) {
                        ontologyVersionIRIField.setText(versionIRIString);
                    }
                } else {
                    ontologyVersionIRIField.setText("");
                    if (ontologyIRI.isPresent()) {
                        ontologyVersionIRIField.setGhostText("e.g. " + ontologyIRIString + (ontologyIRIString.endsWith("/") ? "1.0.0" : "/1.0.0"));
                    }
                }
            }
        } finally {
            updatingViewFromModel = false;
        }
    }

    /**
     * Updates the model from the view - unless the changes in the view were triggered by changes in the model.
     */


    private void updateModelFromView() {
        if (updatingViewFromModel) {
            return;
        }
        try {
            updatingModelFromView = true;
            OWLOntologyID nextId = createOWLOntologyIDFromView();
            OWLOntologyID currentId = activeOntology().getOntologyID();
            if (nextId != null && !currentId.equals(nextId)) {
                updateEmptyPrefixIfNecessary(currentId, nextId);
                getOWLModelManager().applyChange(new SetOntologyID(activeOntology(), nextId));
            }
        } finally {
            updatingModelFromView = false;
        }

    }

    private void updateEmptyPrefixIfNecessary(OWLOntologyID currentId, OWLOntologyID nextId) {
        OWLDocumentFormat format = getOWLModelManager().getOWLOntologyManager().getOntologyFormat(activeOntology());
        if (!(format instanceof PrefixDocumentFormat)) {
            return;
        }
        PrefixDocumentFormat prefixFormat = (PrefixDocumentFormat) format;
        Optional<IRI> currentOntologyIri = currentId.getOntologyIRI();
        if (!currentOntologyIri.isPresent()) {
            return;
        }
        Optional<IRI> nextOntologyIri = nextId.getOntologyIRI();
        if (!nextOntologyIri.isPresent()) {
            return;
        }
        String emptyPrefix = prefixFormat.getDefaultPrefix();
        if (emptyPrefix == null) {
            return;
        }
        if (!isPrefixDerivedFromOntologyIri(currentOntologyIri.get(), emptyPrefix)) {
            return;
        }
        String nextEmptyPrefix = nextOntologyIri.get() + "/";
        prefixFormat.setDefaultPrefix(nextEmptyPrefix);
    }

    private static boolean isPrefixDerivedFromOntologyIri(IRI ontologyIri, String prefix) {
        return (ontologyIri.toString() + "/").equals(prefix);
    }

    private OWLOntology activeOntology() {
        return getOWLModelManager().getActiveOntology();
    }


    private OWLOntologyID createOWLOntologyIDFromView() {
        try {
            ontologyIRIField.clearErrorMessage();
            ontologyIRIField.clearErrorLocation();
            String ontologyIRIString = ontologyIRIField.getText().trim();
            if (ontologyIRIString.isEmpty()) {
                return new OWLOntologyID();
            }
            URI ontURI = new URI(ontologyIRIString);
            IRI ontologyIRI = IRI.create(ontURI);
            String versionIRIString = ontologyVersionIRIField.getText().trim();
            if (versionIRIString.isEmpty()) {
                return new OWLOntologyID(Optional.of(ontologyIRI), Optional.absent());
            }

            URI verURI = new URI(versionIRIString);
            IRI versionIRI = IRI.create(verURI);
            return new OWLOntologyID(Optional.of(ontologyIRI), Optional.of(versionIRI));
        } catch (URISyntaxException e) {
            ontologyIRIField.setErrorMessage(e.getReason());
            ontologyIRIField.setErrorLocation(e.getIndex());
            return null;
        }
    }


  /*  private void myHandleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            change.accept(new OWLOntologyChangeVisitorAdapter() {
                @Override
                public void visit(SetOntologyID change) {
                    try {
                        myUpdateView();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
*/

  /*  private void myUpdateView() throws IOException {

        OntologyMetadataContainer root = new OntologyMetadataContainer();
        BundleContext context = FrameworkUtil.getBundle(OWLOntologyMetadataView.class).getBundleContext();
        URL url = context.getBundle().getResource("mod-v2.0_profile.ttl");

        Set<MODProperty> properties = new HashSet<MODProperty>();
        try {
            properties = root.getModProperties(url.openConnection().getInputStream());
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }


        Set<OWLAnnotation> annots = activeOntology().getAnnotations();


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1, 25, 25));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));


        for (MODProperty prop : properties) {
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));


            JPanel labelDesc = new JPanel(new GridLayout(1, 0, 5, 5));
            JLabel label = new JLabel("<html>" + prop.getLabel().toString() + "</html>");
            label.setFont(new Font("Serif", Font.PLAIN, 14));

            //label.setPreferredSize(new Dimension(80,100));
            label.setForeground(Color.BLACK);
            JLabel description = new JLabel("<html>" + prop.getDescription() + "</html>");
            description.setFont(new Font("Serif", Font.PLAIN, 14));
            labelDesc.add(label);
            labelDesc.add(description);
            //panel.add(labelDesc,BorderLayout.NORTH);
            //panel.add(label);
            //panel.add(description,BorderLayout.EAST);
            panel.add(labelDesc);
            JPanel areaEdit = new JPanel(new GridLayout(1, 0, 5, 5));
            JTextArea area = new JTextArea("Metadata Value");
            for (OWLAnnotation annot : activeOntology().getAnnotations()) {
                if (annot.getProperty().getIRI().toURI().toString().equals(prop.getIRI())) {
                    area = new JTextArea(annot.toString());
                }
            }
            JButton save = new JButton("Save");
            JTextArea finalArea = area;
            save.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    String value = finalArea.getText();
                    finalArea.setText(value);
                }
            });
            areaEdit.add(finalArea);
            //areaEdit.add(save,BorderLayout.LINE_END);
            areaEdit.setBackground(Color.white);
            areaEdit.setPreferredSize(new Dimension(50, 300));
            panel.add(areaEdit);
            JPanel eqProp = new JPanel();
            eqProp.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
            JLabel equivalentProperties = new JLabel("Equivalent Properties");
            equivalentProperties.setFont(new Font(Font.SERIF, Font.ITALIC, 15));
            eqProp.add(equivalentProperties);
            eqProp.setBackground(Color.white);
            for (OWLPropertyExpression dataProp : prop.getEquivalentProperty()) {
                JRadioButton button = new JRadioButton(dataProp.toString());
                button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                eqProp.add(button);
            }
            panel.add(eqProp, BorderLayout.SOUTH);
            panel.setBackground(Color.white);
            panel.setPreferredSize(new Dimension(100, 300));
            mainPanel.add(panel);
        }
        for (OWLAnnotation annot : annots) {
            for (MODProperty property : properties) {
                if (!annot.getProperty().getIRI().toURI().toString().equals(property.getIRI())) {
                    JPanel panel = new JPanel(new GridLayout(0, 1));
                    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

                    JPanel labelDesc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                    JLabel label = new JLabel(annot.getProperty().toString());
                    label.setFont(new Font("Serif", Font.PLAIN, 14));
                    label.setForeground(Color.BLACK);
                    labelDesc.add(label);

                    panel.add(labelDesc, BorderLayout.NORTH);
                    JPanel areaEdit = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                    JLabel area = new JLabel();
                    area = new JLabel(annot.getValue().toString());
                    // area.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    JButton edit = new JButton("Edit Value");
                    JLabel finalArea = area;
                    edit.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            String value = JOptionPane.showInputDialog("Enter your value");
                            finalArea.setText(value);
                        }
                    });
                    areaEdit.add(finalArea);
                    areaEdit.add(edit);
                    panel.add(areaEdit, BorderLayout.CENTER);
                    panel.add(edit);
                    panel.setBackground(Color.WHITE);
                    mainPanel.add(panel);
                }
                break;
            }
        }
        add(mainPanel);
        JScrollPane scroll = new JScrollPane(mainPanel, VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scroll);
        listener = event -> {
            try {
                myHandleModelManagerChangeEvent(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        getOWLModelManager().addListener(listener);

        getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);


        updateViewFromModel();
    }
*/

    protected void disposeOWLView() {
        //list.dispose();
        //getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
        getOWLModelManager().removeListener(listener);
        // getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
    }

}

