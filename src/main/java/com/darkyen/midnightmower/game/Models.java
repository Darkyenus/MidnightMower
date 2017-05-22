package com.darkyen.midnightmower.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.darkyen.midnightmower.gl.Model;

/**
 *
 */
public final class Models {

    private static final G3dModelLoader MODEL_LOADER = new G3dModelLoader(new JsonReader());
    public static final Array<Model> LOADED_MODELS = new Array<>();

    private static void fixMaterials(ModelData model) {
        for (ModelMaterial material : model.materials) {
            if (!material.id.toLowerCase().contains("water")) {
                material.shininess = 0f;
            }
            if (material.ambient.r == 0f && material.ambient.g == 0f && material.ambient.b == 0f) {
                material.ambient.set(material.diffuse).mul(0.5f);
            }
        }
    }

    public static Model load(String name) {
        final ModelData modelData = MODEL_LOADER.loadModelData(Gdx.files.internal("models/" + name + ".g3dj"), null);
        fixMaterials(modelData);
        final Model model = new Model(modelData, 256, false);
        LOADED_MODELS.add(model);
        return model;
    }

    //region Terrain
    public static final Model GrassTile = load("GrassTile");
    public static final Model Cliff = load("StoneCliff");
    public static final Model CliffCorner = load("StoneCliffCorner");
    public static final Model Overhang = load("GrassOverhang");
    public static final Model OverhangCorner = load("GrassOverhangCorner");
    public static final Model TallGrass01 = load("TallGrass01");
    public static final Model TallGrass02 = load("TallGrass02");
    //endregion

    //region Game
    public static final Model LawnMower = load("LawnMower");
    //endregion
    
}
