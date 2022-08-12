package edu.stanford.bmir.protege.examples.tab;

import org.protege.editor.core.ui.workspace.TabbedWorkspace;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

public class MetadataTab extends OWLWorkspaceViewsTab {
    private static final Logger log = LoggerFactory.getLogger(MetadataTab.class);

    @Override
    public void initialise() {
        super.initialise();
        log.info("Metadata tab initialized");
    }

    @Override
    public void dispose() {
        super.dispose();
        log.info("Metadata tab disposed");
    }

}   

