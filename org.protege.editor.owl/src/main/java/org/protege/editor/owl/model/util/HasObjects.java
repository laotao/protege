package org.protege.editor.owl.model.util;

import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 06/03/15
 */
public interface HasObjects<T> {

    Set<T> getObjects();
}
