# React Native Album List

A library for getting all the titles of video albums and videos.
This library was taken https://github.com/aspidvip/react-native-album-list and modified for further work.

# Installation

Install the package from npm:

`yarn add --save react-native-video-albums` or `npm i --save react-native-video-albums`

and

`react-native link`

# Example

`import AlbumsList from 'react-native-video-albums'`

Get a list of video albums

```js
AlbumsList.getAlbumList().then(list => console.log(list));
```

Get a list of videos

```js
AlbumsList.getVideoList({
  title: true,
  name: false,
  size: true,
  description: true,
  location: false,
  date: true,
  resolution: true,
  type: false,
  album: true,
  dimensions: false
}).then(list => console.log(list));
```

### videoListOptions options

| Attribute     | Values             |
| ------------- | ------------------ |
| `title`       | `'true'`/`'false'` |
| `name`        | `'true'`/`'false'` |
| `size`        | `'true'`/`'false'` |
| `description` | `'true'`/`'false'` |
| `location`    | `'true'`/`'false'` |
| `date`        | `'true'`/`'false'` |
| `resolution`  | `'true'`/`'false'` |
| `type`        | `'true'`/`'false'` |
| `album`       | `'true'`/`'false'` |
| `dimensions`  | `'true'`/`'false'` |
