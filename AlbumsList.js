import { NativeModules } from "react-native";

type videoListOptions = {
  title: ?boolean,
  name: ?boolean,
  size: ?boolean,
  description: ?boolean,
  location: ?boolean,
  date: ?boolean,
  resolution: ?boolean,
  type: ?boolean,
  album: ?boolean,
  dimensions: ?boolean
};

export default {
  getVideoList(options: videoListOptions = {}) {
    return NativeModules.RNAlbumsModule.getVideoList(options);
  },

  getVideosByAlbum(options: videoListOptions = {}, albumName) {
    return NativeModules.RNAlbumsModule.getVideosByAlbum(options, albumName);
  },

  getAlbumList() {
    return NativeModules.RNAlbumsModule.getAlbumList();
  },
  deleteVideoFromMediastore(path, callback) {
    return NativeModules.RNAlbumsModule.deleteVideoFromMediastore(path, callback);
  }

};
