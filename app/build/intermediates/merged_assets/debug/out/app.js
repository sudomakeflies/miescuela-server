const NodeMediaServer = require('node-media-server');

const config = {
	rtmp: {
		port: 9935,
		chunk_size: 60000,
		gop_cache: true,
		ping: 30,
		ping_timeout: 60
	},
	http: {
		port: 8000,
		mediaroot: '/data/data/co.miescuela/files/home/storage/shared/miescuela-koala/static/videos',
		allow_origin: '*'
	},
	trans: {
		ffmpeg: '/data/data/co.miescuela/files/usr/bin/ffmpeg',
		tasks: [
			{
			app: 'live',
			hls: true,
			hlsFlags: '[hls_time=2:hls_list_size=3:hls_flags=delete_segments]',
			dash: true,
			dashFlags: '[f=dash:window_size=3:extra_window_size=5]'
			}
		]
	}
};

var nms = new NodeMediaServer(config)
nms.run();