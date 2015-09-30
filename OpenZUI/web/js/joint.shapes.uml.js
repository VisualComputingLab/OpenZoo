/*! JointJS v0.9.3 - JavaScript diagramming library  2015-02-03 
 
 
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this
 file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
if (typeof exports === 'object') {

    var joint = {
        util: require('../src/core').util,
        shapes: {
            basic: require('./joint.shapes.basic')
        },
        dia: {
            ElementView: require('../src/joint.dia.element').ElementView,
            Link: require('../src/joint.dia.link').Link
        }
    };
    var _ = require('lodash');
}

joint.shapes.uml = {}

joint.shapes.uml.Class = joint.shapes.basic.Generic.extend({
    markup: [
        '<g class="rotatable">',
        '<g class="scalable">',
        '<rect class="uml-class-name-rect"/><rect class="uml-class-attrs-rect"/><rect class="uml-class-methods-rect"/>',
        '</g>',
        '<text class="uml-class-name-text"/><text class="uml-class-attrs-text"/><text class="uml-class-methods-text"/>',
        '</g>'
    ].join(''),
    defaults: joint.util.deepSupplement({
        type: 'uml.Class',
        attrs: {
            rect: {'width': 200},
            '.uml-class-name-rect': {'stroke': 'black', 'stroke-width': 2, 'fill': '#3498db'},
            '.uml-class-attrs-rect': {'stroke': 'black', 'stroke-width': 2, 'fill': '#2980b9'},
            '.uml-class-methods-rect': {'stroke': 'black', 'stroke-width': 2, 'fill': '#2980b9'},
            '.uml-class-name-text': {
                'ref': '.uml-class-name-rect', 'ref-y': .5, 'ref-x': .5, 'text-anchor': 'middle', 'y-alignment': 'middle', 'font-weight': 'bold',
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            },
            '.uml-class-attrs-text': {
                'ref': '.uml-class-attrs-rect', 'ref-y': 5, 'ref-x': 5,
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            },
            '.uml-class-methods-text': {
                'ref': '.uml-class-methods-rect', 'ref-y': 5, 'ref-x': 5,
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            }
        },
        name: [],
        attributes: [],
        methods: []

    }, joint.shapes.basic.Generic.prototype.defaults),
    initialize: function() {

        this.on('change:name change:attributes change:methods', function() {
            this.updateRectangles();
            this.trigger('uml-update');
        }, this);

        this.updateRectangles();

        joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
    },
    getClassName: function() {
        return this.get('name');
    },
    updateRectangles: function() {

        var attrs = this.get('attrs');

        var rects = [
            {type: 'name', text: this.getClassName()},
            {type: 'attrs', text: this.get('attributes')},
            {type: 'methods', text: this.get('methods')}
        ];

        var offsetY = 0;

        _.each(rects, function(rect) {

            var lines = _.isArray(rect.text) ? rect.text : [rect.text];
            var rectHeight = lines.length * 20 + 20;

            attrs['.uml-class-' + rect.type + '-text'].text = lines.join('\n');
            attrs['.uml-class-' + rect.type + '-rect'].height = rectHeight;
            attrs['.uml-class-' + rect.type + '-rect'].transform = 'translate(0,' + offsetY + ')';

            offsetY += rectHeight;
        });
    }

});

joint.shapes.uml.ClassView = joint.dia.ElementView.extend({
    initialize: function() {

        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

        this.listenTo(this.model, 'uml-update', function() {
            this.update();
            this.resize();
        });
    }
});

joint.shapes.uml.Abstract = joint.shapes.uml.Class.extend({
    defaults: joint.util.deepSupplement({
        type: 'uml.Abstract',
        attrs: {
            '.uml-class-name-rect': {fill: '#e74c3c'},
            '.uml-class-attrs-rect': {fill: '#c0392b'},
            '.uml-class-methods-rect': {fill: '#c0392b'}
        }
    }, joint.shapes.uml.Class.prototype.defaults),
    getClassName: function() {
        return ['<<Abstract>>', this.get('name')];
    }

});

joint.shapes.uml.AbstractView = joint.shapes.uml.ClassView;

joint.shapes.uml.Interface = joint.shapes.uml.Class.extend({
    defaults: joint.util.deepSupplement({
        type: 'uml.Interface',
        attrs: {
            '.uml-class-name-rect': {fill: '#f1c40f'},
            '.uml-class-attrs-rect': {fill: '#f39c12'},
            '.uml-class-methods-rect': {fill: '#f39c12'}
        }
    }, joint.shapes.uml.Class.prototype.defaults),
    getClassName: function() {
        return ['<<Interface>>', this.get('name')];
    }

});
joint.shapes.uml.InterfaceView = joint.shapes.uml.ClassView;

joint.shapes.uml.Generalization = joint.dia.Link.extend({
    defaults: {
        type: 'uml.Generalization',
        attrs: {'.marker-target': {d: 'M 20 0 L 0 10 L 20 20 z', fill: 'white'}}
    }
});

joint.shapes.uml.Implementation = joint.dia.Link.extend({
    defaults: {
        type: 'uml.Implementation',
        attrs: {
            '.marker-target': {d: 'M 20 0 L 0 10 L 20 20 z', fill: 'white'},
            '.connection': {'stroke-dasharray': '3,3'}
        }
    }
});

joint.shapes.uml.Aggregation = joint.dia.Link.extend({
    defaults: {
        type: 'uml.Aggregation',
        attrs: {'.marker-target': {d: 'M 40 10 L 20 20 L 0 10 L 20 0 z', fill: 'white'}}
    }
});

joint.shapes.uml.Composition = joint.dia.Link.extend({
    defaults: {
        type: 'uml.Composition',
        attrs: {'.marker-target': {d: 'M 40 10 L 20 20 L 0 10 L 20 0 z', fill: 'black'}}
    }
});

joint.shapes.uml.Association = joint.dia.Link.extend({
    defaults: {type: 'uml.Association'}
});

// Statechart

joint.shapes.uml.State = joint.shapes.basic.Generic.extend({
    markup: [
        '<g class="rotatable">',
        '<g class="scalable">',
        '<rect class="uml-state-body"/>',
        '</g>',
        '<path class="uml-state-separator"/>',
        '<text class="uml-state-name"/>',
        '<text class="uml-state-events"/>',
        '</g>',
        /* 
         * dimsam custom remove button
         * http://stackoverflow.com/questions/30153345/how-to-give-jointjs-elements-a-remove-tool
         */
        '<g class="element-tools">',
        '<g class="element-tool-remove" event="remove"><circle fill="red" r="8"/>',
        '<path transform="scale(.6) translate(-16, -16)" d="M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 16.447,18.746 21.948,24.248z"/>',
        '<title>Remove this element from the model</title>',
        '</g>',
        '</g>'
    ].join(''),
    defaults: joint.util.deepSupplement({
        type: 'uml.State',
        attrs: {
            '.uml-state-body': {
                'width': 200, 'height': 200, 'rx': 10, 'ry': 10,
                'fill': '#ecf0f1', 'stroke': '#bdc3c7', 'stroke-width': 3
            },
            '.uml-state-separator': {
                'stroke': '#bdc3c7', 'stroke-width': 2
            },
            '.uml-state-name': {
                'ref': '.uml-state-body', 'ref-x': .5, 'ref-y': 5, 'text-anchor': 'middle',
                'fill': '#000000', 'font-family': 'Courier New', 'font-size': 14
            },
            '.uml-state-events': {
                'ref': '.uml-state-separator', 'ref-x': 5, 'ref-y': 5,
                'fill': '#000000', 'font-family': 'Courier New', 'font-size': 14
            }
        },
        name: 'State',
        events: []

    }, joint.shapes.basic.Generic.prototype.defaults),
    initialize: function() {

        this.on({
            'change:name': this.updateName,
            'change:events': this.updateEvents,
            'change:size': this.updatePath
        }, this);

        this.updateName();
        this.updateEvents();
        this.updatePath();

        joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
    },
    updateName: function() {

        this.attr('.uml-state-name/text', this.get('name'));
    },
    updateEvents: function() {

        this.attr('.uml-state-events/text', this.get('events').join('\n'));
    },
    updatePath: function() {

        var d = 'M 0 20 L ' + this.get('size').width + ' 20';

        // We are using `silent: true` here because updatePath() is meant to be called
        // on resize and there's no need to to update the element twice (`change:size`
        // triggers also an update).
        this.attr('.uml-state-separator/d', d, {silent: true});
    }

});

joint.shapes.uml.StartState = joint.shapes.basic.Circle.extend({
    defaults: joint.util.deepSupplement({
        markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g>',
        type: 'uml.StartState',
        attrs: {circle: {'fill': '#34495e', 'stroke': '#2c3e50', 'stroke-width': 2, 'rx': 1}}

    }, joint.shapes.basic.Circle.prototype.defaults)

});

joint.shapes.uml.EndState = joint.shapes.basic.Generic.extend({
    markup: '<g class="rotatable"><g class="scalable"><circle class="outer"/><circle class="inner"/></g></g>',
    defaults: joint.util.deepSupplement({
        type: 'uml.EndState',
        size: {width: 20, height: 20},
        attrs: {
            'circle.outer': {
                transform: 'translate(10, 10)',
                r: 10,
                fill: '#ffffff',
                stroke: '#2c3e50'
            },
            'circle.inner': {
                transform: 'translate(10, 10)',
                r: 6,
                fill: '#34495e'
            }
        }

    }, joint.shapes.basic.Generic.prototype.defaults)

});

var ClickableView = joint.dia.ElementView.extend({
    pointerdown: function() {
        this._click = true;
        joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
    },
    pointermove: function() {
        this._click = false;
        joint.dia.ElementView.prototype.pointermove.apply(this, arguments);
    },
    pointerup: function(evt, x, y) {
        if (this._click) {
            this.notify('cell:click', evt, x, y);
        } else {
            // dimsam custom remove button action
            var className = evt.target.parentNode.getAttribute('class');
            if (className === 'element-tool-remove') {
                this.model.remove();
            }
            joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
        }
    },
    pointerclick: function(evt, x, y) {
        // dimsam custom remove button action -- pointclick action caught depends on browser
        var className = evt.target.parentNode.getAttribute('class');
        if (className === 'element-tool-remove') {
            this.model.remove();
        }
        joint.dia.ElementView.prototype.pointerclick.apply(this, arguments);

    }
});


// joint.shapes.uml.Transition2way = joint.dia.Link.extend({
//     defaults: {
//         type: 'uml.Transition',
//         attrs: {
//             '.marker-source': { d: 'M 10 0 L 0 5 L 10 10 z', fill: '#34495e', stroke: '#2c3e50' },
//             '.marker-target': { d: 'M 10 0 L 0 5 L 10 10 z', fill: '#34495e', stroke: '#2c3e50' },
//             '.connection': { stroke: '#2c3e50' }
//         }
//     }
// });


joint.shapes.uml.Transition = joint.dia.Link.extend({
    defaults: {
        type: 'uml.Transition',
        attrs: {
            '.marker-target': {d: 'M 10 0 L 0 5 L 10 10 z', fill: '#34495e', stroke: '#2c3e50'},
            '.connection': {stroke: '#2c3e50'}
        }
    }
});

if (typeof exports === 'object') {

    module.exports = joint.shapes.uml;
}
