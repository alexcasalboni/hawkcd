package net.hawkengine.core.materialupdater;

import net.hawkengine.model.enums.MaterialType;

public final class MaterialUpdaterFactory {
    public static MaterialUpdater create(MaterialType materialType) {
        switch (materialType) {
            case GIT:
                return new GitMaterialUpdater();
            case NUGET:
                return new NuGetMaterialUpdater();
            default:
                return null;
        }
    }
}