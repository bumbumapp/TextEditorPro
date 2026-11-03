package net.gsantner.markor.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SampleDocsTest {
    @Test
    public void sampleDocsShowFeaturesAndStayPermissionFree() {
        assertThat(SampleDocs.WELCOME_MD).startsWith("# ").contains("without any permission").contains("- [ ]");
        assertThat(SampleDocs.TODO_TXT.split("\n")).hasSizeGreaterThanOrEqualTo(4);
        assertThat(SampleDocs.TODO_TXT).contains("(A) ").contains("x 2026");
        assertThat(SampleDocs.QUICKNOTE_MD).startsWith("# QuickNote");
    }
}
