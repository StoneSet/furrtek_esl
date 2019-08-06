
# ESL EMITTER HACK - V1 of 2014

### The Eagle project of the esl project + apk and hex files

**(c) Found the entire project here (not the v1 but u can found it on the web archive): http://furrtek.free.fr/?a=esl**

### TO DO

- Web version of the app (Capability to change page)
- Decompile the app ✅ (Now we will spend many hours to understand the code...)

## Included :

- All needed files to modify the project
- BOM (in /pcb/)
- Prog files & source (not commented) for the APK (in /android/apk/)

## To use it :

1. Buy the pcb then solder every composants.
2. Flash the Attiny85 with phxtx1.hex with these fuses :

- Low: *0xEF*, High: *0xDD*, Extended: *0xFF*

4. Install [pricehax15.apk](https://github.com/StoneSet/furrtek_esl/blob/master/apk/pricehax15.apk  "pricehax15.apk") on your phone (only Android at this time)
5. Plug the jack cable in your phone, launch the app and vouala !

## Some img's :

<img src="https://raw.githubusercontent.com/StoneSet/furrtek_esl/master/img/schematics.png" alt="alt" width="600">

------
<img src="http://files.stoneset.fr/others/esl_furrtek/img/docs/IMG_0121.JPG" alt="alt" width="500">

### To dot it, you will need :

<img src="http://files.stoneset.fr/others/esl_furrtek/img/docs/IMG_0125.JPG" alt="alt" width="400">

| Components | list |
|--|--|
| x1 Attiny85 | x1 78L05 regulator |
| x2 100µF capacitor | x1 10nF ceramic capacitor |
| x2 22pF ceramic capacitor | x1 3.3nF ceramic capacitor |
| x1 10k resistor | x1 100k resistor |
| x1 3.3k resistor | x1 10MHz quartz |
| x1 220ohm resistor | x1 47ohm resistor |
| x1 SFH485P IR LED - 880NM | x1 9v battery clip |
| x1 2N2222 transistor | x1 SPDT Switch |
| x1 A randomly chosen diode |

This version work finely !

#

(c) @furrtek & @StoneSet & @lefuturiste - http://furrtek.free.fr/?a=esl - http://stoneset.github.io/ (2019)

*Prog infos etc here* : [https://github.com/Jonas1312/Pricehax](https://github.com/Jonas1312/Pricehax)

plz do not steal, i'm not the owner of this project, i'm only the owner of this pcb. Have fun !