package com.darkyen.pv112game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.darkyen.pv112game.gl.Model;

/**
 *
 */
public class Models {

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
        final Model model = new Model(modelData);
        LOADED_MODELS.add(model);
        return model;
    }

    //region Cliffs
    public static final Model BrownCliff01 = load("Brown_Cliff_01");
    public static final Model BrownCliffBottom01 = load("Brown_Cliff_Bottom_01");
    public static final Model BrownCliffBottomCorner01 = load("Brown_Cliff_Bottom_Corner_01");
    public static final Model BrownCliffCorner01 = load("Brown_Cliff_Corner_01");
    public static final Model BrownCliffTop01 = load("Brown_Cliff_Top_01");
    public static final Model BrownCliffTopCorner01 = load("Brown_Cliff_Top_Corner_01");
    public static final Model BrownWaterfall01 = load("Brown_Waterfall_01");
    public static final Model BrownWaterfallTop01 = load("Brown_Waterfall_Top_01");
    
    public static final Model GreyCliff01 = load("Grey_Cliff_01");
    public static final Model GreyCliffBottom01 = load("Grey_Cliff_Bottom_01");
    public static final Model GreyCliffBottomCorner01 = load("Grey_Cliff_Bottom_Corner_01");
    public static final Model GreyCliffCorner01 = load("Grey_Cliff_Corner_01");
    public static final Model GreyCliffTop01 = load("Grey_Cliff_Top_01");
    public static final Model GreyCliffTopCorner01 = load("Grey_Cliff_Top_Corner_01");
    public static final Model GreyWaterfall01 = load("Grey_Waterfall_01");
    public static final Model GreyWaterfallTop01 = load("Grey_Waterfall_Top_01");
    //endregion

    //region Plates
    public static final Model PlateGrass01 = load("Plate_Grass_01");
    public static final Model PlateGrassDirt01 = load("Plate_Grass_Dirt_01");

    public static final Model PlateRiver01 = load("Plate_River_01");
    public static final Model PlateRiverCorner01 = load("Plate_River_Corner_01");
    public static final Model PlateRiverCornerDirt01 = load("Plate_River_Corner_Dirt_01");
    public static final Model PlateRiverDirt01 = load("Plate_River_Dirt_01");
    //endregion

    //region Trees & Trunks
    public static final Model LargeOakDark01 = load("Large_Oak_Dark_01");
    public static final Model LargeOakFall01 = load("Large_Oak_Fall_01");
    public static final Model LargeOakGreen01 = load("Large_Oak_Green_01");
    public static final Model OakDark01 = load("Oak_Dark_01");
    public static final Model OakFall01 = load("Oak_Fall_01");
    public static final Model OakGreen01 = load("Oak_Green_01");
    public static final Model Tree01 = load("Tree_01");
    public static final Model Tree02 = load("Tree_02");

    public static final Model Trunk01 = load("Trunk_01");
    public static final Model Trunk02 = load("Trunk_02");
    public static final Model TrunkAlt01 = load("Trunk_Alt_01");
    public static final Model TrunkAlt02 = load("Trunk_Alt_02");
    public static final Model FallenTrunk01 = load("Fallen_Trunk_01");
    //endregion

    //region Plants
    public static final Model Grass01 = load("Grass_01");
    public static final Model HangingMoss01 = load("Hanging_Moss_01");

    public static final Model MushroomBrown01 = load("Mushroom_Brown_01");
    public static final Model MushroomRed01 = load("Mushroom_Red_01");
    public static final Model MushroomTall01 = load("Mushroom_Tall_01");

    public static final Model FlowerRed01 = load("Flower_Red_01");
    public static final Model FlowerTallRed01 = load("Flower_Tall_Red_01");
    public static final Model FlowerTallYellow01 = load("Flower_Tall_Yellow_01");
    public static final Model FlowerTallYellow02 = load("Flower_Tall_Yellow_02");
    public static final Model FlowerYellow01 = load("Flower_Yellow_01");
    public static final Model FlowerYellow02 = load("Flower_Yellow_02");

    public static final Model Plant101 = load("Plant_1_01");
    public static final Model Plant201 = load("Plant_2_01");
    public static final Model Plant301 = load("Plant_3_01");
    public static final Model Waterlily01 = load("Water_lily_01");
    //endregion

    //region Rocks
    public static final Model Rock101 = load("Rock_1_01");
    public static final Model Rock201 = load("Rock_2_01");
    public static final Model Rock301 = load("Rock_3_01");
    public static final Model Rock401 = load("Rock_4_01");
    public static final Model Rock501 = load("Rock_5_01");
    public static final Model Rock601 = load("Rock_6_01");
    public static final Model TallRock101 = load("Tall_Rock_1_01");
    public static final Model TallRock201 = load("Tall_Rock_2_01");
    public static final Model TallRock301 = load("Tall_Rock_3_01");
    //endregion

    //region Structures
    public static final Model Campfire01 = load("Campfire_01");

    public static final Model Tent01 = load("Tent_01");
    public static final Model TentPoles01 = load("Tent_Poles_01");

    public static final Model WoodFence01 = load("Wood_Fence_01");
    public static final Model WoodFenceBroken01 = load("Wood_Fence_Broken_01");
    public static final Model WoodFenceGate01 = load("Wood_Fence_Gate_01");
    //endregion

    //region Terrain
    public static final Model GrassTile = load("Tile");
    public static final Model Cliff = load("Cliff");
    public static final Model CliffCorner = load("CliffCorner");
    public static final Model Overhang = load("Overhang");
    public static final Model OverhangCorner = load("OverhangCorner");
    //endregion

    //region Game
    public static final Model LawnMower = load("LawnMower");
    //endregion
    
}
