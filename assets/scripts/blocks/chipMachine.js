const library = require("base/library");
const myItems = require("items");
const ItemImage = Packages.mindustry.ui.ItemImage;
const chipMachine = library.MultiCrafter(GenericCrafter, GenericCrafter.GenericCrafterBuild, "chipMachine", [
  {
    input: {
    items: ["magic-phantomSteel/1","magic-phantomTitaniumSteel/1","magic-mysticCrystal/1","silicon/3"],
    },
    output: {
      items: ["magic-phantomSteel/1"],
    },
    craftTime: 30,
  }, 
  /*{
    input: {
    items: ["magic-矿渣桶/1"],
    },
    output: {
      liquids: ["slag/60"],
    },
    craftTime: 30,
  },
  {
    input: {
    items: ["magic-冷冻液桶/1"],
    },
    output: {
      liquids: ["cryofluid/60"],
    },
    craftTime: 30,
  },*/
  
]);