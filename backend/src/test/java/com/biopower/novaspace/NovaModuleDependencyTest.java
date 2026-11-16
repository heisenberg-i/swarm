package com.biopower.novaspace;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.biopower", importOptions = ImportOption.DoNotIncludeTests.class)
class NovaModuleDependencyTest {

    @ArchTest
    static final ArchRule novaDoesNotCallIoTIngest = noClasses()
            .that().resideInAPackage("com.biopower.novaspace..")
            .should().dependOnClassesThat().haveSimpleName("IoTController");

    @ArchTest
    static final ArchRule novaDoesNotCallHmiCommands = noClasses()
            .that().resideInAPackage("com.biopower.novaspace..")
            .should().dependOnClassesThat().haveSimpleName("HmiController");
}
