package com.redhat.documentation.asciidoc.extension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReaderPreprocessorTest {
    private Asciidoctor asciidoctor;
    private JavaExtensionRegistry registry;
    private OptionsBuilder optionsBuilder;
    private Map<String, Object> processorConfig;

    @BeforeEach
    private void setup() {
        optionsBuilder = OptionsBuilder.options();
        asciidoctor = Asciidoctor.Factory.create();
        registry = asciidoctor.javaExtensionRegistry();


        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);
    }

    @Test
    public void testIfDefSectionRemoval() throws URISyntaxException {
        var readerPreprocessor = new ReaderPreprocessor();

        registry.preprocessor(readerPreprocessor);

        var adoc = new File(this.getClass().getClassLoader().getResource("docs/preprocess/remove-ifdef-section.adoc").toURI());
        var doc = asciidoctor.loadFile(adoc, optionsBuilder.asMap());
        var lines = readerPreprocessor.getLines();

        assertThat(lines).doesNotContain("ifdef::");
        assertThat(lines).doesNotContain("endif::");
        assertThat(lines).doesNotContain("This section should be removed.");
    }

    @Test
    public void testSingleLineInclusion() throws URISyntaxException {
        var readerPreprocessor = new ReaderPreprocessor();

        registry.preprocessor(readerPreprocessor);

        var adoc = new File(this.getClass().getClassLoader().getResource("docs/preprocess/single-line-ifdef.adoc").toURI());
        var doc = asciidoctor.loadFile(adoc, optionsBuilder.asMap());
        var lines = readerPreprocessor.getLines();

        assertThat(lines).doesNotContain("ifdef::");
        assertThat(lines).doesNotContain("endif::");
        assertThat(lines).doesNotContain("This section should be removed.");
        assertThat(lines).contains("ifdef::context[:parent-context: {context}]");
    }

    @Test
    public void testIfDefDeclarationRemoval() throws URISyntaxException {
        var readerPreprocessor = new ReaderPreprocessor();

        registry.preprocessor(readerPreprocessor);

        var adoc = new File(this.getClass().getClassLoader().getResource("docs/preprocess/remove-ifdef-line.adoc").toURI());
        var doc = asciidoctor.loadFile(adoc, optionsBuilder.asMap());
        var lines = readerPreprocessor.getLines();

        assertThat(lines).doesNotContain("ifdef::");
        assertThat(lines).doesNotContain("endif::");
        assertThat(lines).contains("This section should not be removed.");
    }

    @Test
    public void testIfEvalInclusion() throws URISyntaxException {
        var readerPreprocessor = new ReaderPreprocessor();

        registry.preprocessor(readerPreprocessor);

        var adoc = new File(this.getClass().getClassLoader().getResource("docs/preprocess/ifeval-test.adoc").toURI());
        var doc = asciidoctor.loadFile(adoc, optionsBuilder.asMap());
        var lines = readerPreprocessor.getLines();

        assertThat(lines).contains("ifeval::[\"{cmdcli}\" == \"oc\"]");
        assertThat(lines).contains("* If using a version of OpenShift earlier than OpenShift 4 the link:https://github.com/coreos/prometheus-operator/tree/master/contrib/kube-prometheus[Prometheus Operator^] and Custom Resource Definitions must be installed.");
        assertThat(lines).contains("endif::[]");
    }
}
