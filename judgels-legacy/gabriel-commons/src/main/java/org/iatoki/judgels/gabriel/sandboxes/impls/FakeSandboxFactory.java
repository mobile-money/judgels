package org.iatoki.judgels.gabriel.sandboxes.impls;

import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.sandboxes.Sandbox;
import org.iatoki.judgels.gabriel.sandboxes.SandboxFactory;
import org.iatoki.judgels.gabriel.sandboxes.SandboxesInteractor;

import java.io.File;
import java.io.IOException;

public class FakeSandboxFactory implements SandboxFactory {

    private final File baseDir;
    private int sandboxesCount;

    public FakeSandboxFactory(File baseDir) {
        this.baseDir = baseDir;
        this.sandboxesCount = 0;
    }

    @Override
    public Sandbox newSandbox() {
        sandboxesCount++;
        File sandboxDir = new File(baseDir, "" + sandboxesCount);

        try {
            FileUtils.forceMkdir(sandboxDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new FakeSandbox(sandboxDir);
    }

    @Override
    public SandboxesInteractor newSandboxesInteractor() {
        return new FakeSandboxesInteractor();
    }
}
