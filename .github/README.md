# CTM Vintage [![Curseforge](http://cf.way2muchnoise.eu/full_1350228_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/ctm-vintage) [![Curseforge](http://cf.way2muchnoise.eu/versions/For%20MC_1350228_all.svg)](https://www.curseforge.com/minecraft/mc-mods/ctm-vintage)

A fork of [Chisel Team's ConnectedTexturesMod](https://www.curseforge.com/minecraft/mc-mods/ctm) with backported features.

Now you can use some new ctm types like ctm\_horizontal in 1.12.2. The json loading system is also backported, though it needs a ctm.json file as index in the mod's domain. The logic format should be the same as the one in modern versions.

For developers, there should not be any resource format changes needed. But you may need to update java codes since some logic is moved to ConnectionCheck. Also, IFacade now supports custom connection block state. Implement it if needed.

It is incompatible with Chisel now. A fork to fix is planned.

Sadly Chisel Team haven't updated their Wiki yet. Here are some examples for reference.

# Json Logic

To load customized json ctm logic, there should be a "ctm.json" file in the mod's root domain like "assets/modid/ctm.json". The "ctm.json" will be loaded like sounds.json, so resource pack can contain their own types without writting the old items from the original file.

assets/ctm/ctm.json in CTM Vintage

`{ "logics": [ "ctm", "optifine_full" ] }`

CTM Vintage will search the items in "assets/modid/ctm\_logic", according to "assets/ctm/ctm.json", and register them ctm type in "modid:item".

You can also write items in sub folders like "mylogic/ctm" and put json logic file in "assets/modid/ctm\_logic/mylogic/ctm.json". It will be "modid:mylogic/ctm" in ctm type.

In the case above, it will search for "assets/ctm/ctm\_logic/ctm.json" "assets/ctm/ctm\_logic/optifine\_full.json", and "ctm:ctm" "ctm:optifine\_full" is registered.

assets/ctm/ctm\_logic/ctm.json in CTM Vintage, copied from the original CTM

`{ "positions": [ {"id": "TOP", "directions": ["up"]}, {"id": "TOP_RIGHT", "directions": ["up","east"]}, {"id": "RIGHT", "directions": ["east"]}, {"id": "BOTTOM_RIGHT", "directions": ["down","east"]}, {"id": "BOTTOM", "directions": ["down"]}, {"id": "BOTTOM_LEFT", "directions": ["down","west"]}, {"id": "LEFT", "directions": ["west"]}, {"id": "TOP_LEFT", "directions": ["up","west"]} ], "submaps": { "": { "type": "grid", "width": 4, "height": 4 }, "original": { "type": "grid", "width": 2, "height": 2 } }, "faces": { "": { "type": "grid", "width": 2, "height": 2 } }, "rules": [ {"output":"0,0", "from":1, "at":"0,0", "connected":["TOP","LEFT","TOP_LEFT"]}, {"output":"1,0", "from":1, "at":"1,0", "connected":["TOP","RIGHT","TOP_RIGHT"]}, {"output":"2,0", "from":1, "at":"0,0", "connected":["TOP"],"unconnected":["LEFT"]}, {"output":"3,0", "from":1, "at":"1,0", "connected":["TOP"],"unconnected":["RIGHT"]}, {"output":"0,1", "from":1, "at":"0,1", "connected":["BOTTOM","LEFT","BOTTOM_LEFT"]}, {"output":"1,1", "from":1, "at":"1,1", "connected":["BOTTOM","RIGHT","BOTTOM_RIGHT"]}, {"output":"2,1", "from":1, "at":"0,1", "connected":["BOTTOM"],"unconnected":["LEFT"]}, {"output":"3,1", "from":1, "at":"1,1", "connected":["BOTTOM"],"unconnected":["RIGHT"]}, {"output":"0,2", "from":1, "at":"0,0", "connected":["LEFT"],"unconnected":["TOP"]}, {"output":"1,2", "from":1, "at":"1,0", "connected":["RIGHT"],"unconnected":["TOP"]}, {"output":"2,2", "from":1, "at":"0,0", "connected":["TOP","LEFT"],"unconnected":["TOP_LEFT"]}, {"output":"3,2", "from":1, "at":"1,0", "connected":["TOP","RIGHT"],"unconnected":["TOP_RIGHT"]}, {"output":"0,3", "from":1, "at":"0,1", "connected":["LEFT"],"unconnected":["BOTTOM"]}, {"output":"1,3", "from":1, "at":"1,1", "connected":["RIGHT"],"unconnected":["BOTTOM"]}, {"output":"2,3", "from":1, "at":"0,1", "connected":["BOTTOM","LEFT"],"unconnected":["BOTTOM_LEFT"]}, {"output":"3,3", "from":1, "at":"1,1", "connected":["BOTTOM","RIGHT"],"unconnected":["BOTTOM_RIGHT"]}, {"output":"original0,0", "from":0, "at":"0,0", "unconnected":["TOP","LEFT"]}, {"output":"original1,0", "from":0, "at":"1,0", "unconnected":["TOP","RIGHT"]}, {"output":"original0,1", "from":0, "at":"0,1", "unconnected":["BOTTOM","LEFT"]}, {"output":"original1,1", "from":0, "at":"1,1", "unconnected":["BOTTOM","RIGHT"]} ] }`

There are changes in optifine\_full.json. The grid is changed to 12x12 (192x192px or multiple), a square. Otherwise, forge will recognize it as an animated texture and stop it from loading.

# Proxy

The proxy loading logic is updated, allowing you to write them like this from Mekanism.

assets/mekanism/textures/block/thermal\_evaporation\_block\_1.png(16x16px)

![image](https://media.forgecdn.net/attachments/description/1350228/description_645bdb00-e60b-4a64-a521-24e70e72d9c9.png)

assets/mekanism/textures/block/thermal\_evaporation\_block\_1.png.mcmeta

`{ "ctm": { "ctm_version": 1, "proxy": "mekanism:ctm/thermal_evaporation_block_1_full", "type": "CTM", "layer": "SOLID", "textures": [ "mekanism:ctm/thermal_evaporation_block_1" ] } }`

assets/mekanism/textures/ctm/thermal\_evaporation\_block\_1.png(16x16px)

![image](https://media.forgecdn.net/attachments/description/1350228/description_f78081d3-e78b-474e-96c4-18f5310df669.png)

assets/mekanism/textures/ctm/thermal\_evaporation\_block\_1\_full.png(192x192px)

![image](https://media.forgecdn.net/attachments/description/1350228/description_afc510ba-775b-4eac-aa63-e9504665f42c.png)

assets/mekanism/textures/ctm/thermal\_evaporation\_block\_1\_full.png.mcmeta

`{ "ctm": { "ctm_version": 1, "type": "ctm:optifine_full" } }`

The final texture will be thermal\_evaporation\_block\_1\_full.png loaded in ctm:optifine\_full.