<p align="center">
  <h1 align="center"> Resisto </h1>
</p>

<p align="center">
  <b>Take a photo for your resistor and you will know its value</b>
</p>

---

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Description](#description)
- [Installation](#installation)
- [Team members roles](#team-members-roles)
- [Steps of our work](#steps-of-our-work)
- [References](#references)
- [Demo video](#demo-video)

## Description
Resisto is a mobile application, you can use it by simply provide an image of your resistor and you will know the value of this resistor from Resisto application.<br />
The Resisto app uses the color key which is provided on the resistor to know its value.<br />

## Installation
download our [apk](https://github.com/ETBMina/Resisto/releases/download/v1.0/Resisto.apk) and install it on your android mobile

## Team members roles
| Member name   |  Role         |
| ------------- | ------------- |
| Beshoy Anwar  | Detect colors and android application |
| Mina Rizk     | Calculate resistor value  |
| Mina Talaat   | Extract colors from resistor body and android application |
| Peter Rateb   | Extract resistor body from image  |

## Steps of our work 
The taken photo will look like that <br />
![takenPhoto](https://github.com/ETBMina/Resisto/blob/master/test/source.jpg) <br />
1- cut the captured photo to get out the inside rectangle only <br />
![cuttedPhoto](https://github.com/ETBMina/Resisto/blob/master/test/cuttedImage.jpg) <br />
2- extract the body of the resistor from the cutted image <br />
![resistorBody](https://github.com/ETBMina/Resisto/blob/master/test/resistorBody.jpg) <br />
3- extract the colors from the body of the resistor, each color in new single image <br />
![color1](https://github.com/ETBMina/Resisto/blob/master/test/extractedColors/ColorNo1.jpg) <br />
![color2](https://github.com/ETBMina/Resisto/blob/master/test/extractedColors/ColorNo2.jpg) <br />
![color3](https://github.com/ETBMina/Resisto/blob/master/test/extractedColors/ColorNo3.jpg) <br />
4- detect the colors in the images which extracts from the resistor body <br />
5- use the colors to calculate the resistance value <br />
![result](https://github.com/ETBMina/Resisto/blob/master/test/result.jpg) <br />

## Demo video
Check our [demo video](https://www.youtube.com/watch?v=di8VYU7bFb0&feature=youtu.be)

## References 
how to calculate the resistor value from its colors from [here](http://www.resistorguide.com/resistor-color-code/)
