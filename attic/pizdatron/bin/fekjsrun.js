//require('source-map-support').install()

//global.React = require('react')
//global.ReactDOMServer = require('react-dom/server')

global.kotlin = require('../out/lib/kotlin.js')
require('../out/fekjs.js')

kotlin.modules.fekjs.fekjs.commandLineRun()

