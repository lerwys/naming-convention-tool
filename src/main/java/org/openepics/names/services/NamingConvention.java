package org.openepics.names.services;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An interface defining the naming convention to be used by the application that includes:
 * - name validation rules
 * - name uniqueness rules
 * - form of composite names
 *
 * The used naming convention is configured through beans.xml using the CDI alternatives mechanism.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public interface NamingConvention {

    /**
     * True if the section name is valid according to convention rules, in the context of other names in the hierarchy
     * leading to the section.
     *
     * @param parentPath the list of section names starting from the root of the hierarchy to the parent of the section
     * for which we are testing the name
     * @param name the name of the section to test for validity
     */
    boolean isSectionNameValid(List<String> parentPath, String name);

    /**
     * True if the device type name is valid according to convention rules, in the context of other names in the
     * hierarchy leading to the device type.
     *
     * @param parentPath the list of device type names starting from the root of the hierarchy to the parent of the
     * device type for which we are testing the name
     * @param name the name of the device type to test for validity
     */
    boolean isDeviceTypeNameValid(List<String> parentPath, String name);

    /**
     * True if the device's instance index is valid according to convention rules, in the context of device's section
     * and device type.
     *
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section
     * containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific
     * subtype of the device
     * @param instanceIndex the device instance index to test for validity, or null if no instance index is assigned to
     * the device, in which case this is also checked for validity
     */
    boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);

    /**
     * The representative of the equivalence class the name belongs to. This is used to ensure uniqueness of names when
     * treating similar looking names (for example, containing 0 vs. O, 1 vs. l) as equal.
     *
     * @param name the name of which to determine the equivalence class
     */
    String equivalenceClassRepresentative(String name);

    /**
     * The convention name of the device defined by it's section, device type and instance index
     *
     * @param sectionPath the list of section names starting from the root of the hierarchy to the specific section
     * containing the device
     * @param deviceTypePath the list of device type names starting from the root of the hierarchy to the specific
     * subtype of the device
     * @param instanceIndex the device instance index. Null if omitted.
     */
    String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex);
}
