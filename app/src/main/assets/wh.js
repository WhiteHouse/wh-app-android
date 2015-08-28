window.alert = function(text) {
    console.log("ALERT: " + text);
    return true;
}

var WhiteHouse = {
    transformFontSize: function(f) {
        var $target = $("div.content");
        var fontSize = parseInt($target.css("font-size"));
        var newFontSize = f(fontSize);
        $target.css("font-size", newFontSize);
    },
    textUp: function() {
        this.transformFontSize(function(x) {
            return x + 5;
        });
    },
    textDown: function() {
        this.transformFontSize(function(x) {
            if (0 < x - 5) {
                return x - 5;
            } else {
                return x;
            }
        });
    }
};

WhiteHouse.loadPage = function(pageInfo) {
    try {
        WhiteHouse.loadPageInternal(pageInfo);
    } catch (e) {
        console.log("Error in loadPage: " + e.name + "; " + e.message);
    }
}


WhiteHouse.loadPageInternal = function(pageInfo) {
    var container = $('#article');
    try {
        var t = _.template(document.getElementById("template").innerText);
        container.html(t(pageInfo));
    } catch (e) {
        alert("Error in template: " + e.name + "; " + e.message);
    }

    $.fn.fixLinks = function(attr) {
        return this.each(function() {
            var url = $(this).attr(attr);
            if (url && url.indexOf("/") == 0) {
                $(this).attr(attr, "http://www.whitehouse.gov/" + url);
            }
        });
    };

    container.find("a").fixLinks("href");
    container.find("img").fixLinks("src");

    container.find("div[style]").each(function(idx, div) {
        div.removeAttribute("style");
    });

    var youTubeElements = [];

    console.log("[YOUTUBE HANDLING] Searching for YouTube videos in <object> and <iframe> elements");

    container.find("object param[name=movie]").each(function(idx, param) {
        console.log("[YOUTUBE HANDLING] Found <param>: " + param);
        var matches = $(param).attr('value').match(/youtube.*\/v\/(\w+)\b/);
        if (matches) {
            var el = $(param).parent();
            console.log("[YOUTUBE HANDLING] Parent <object>(?) element: " + el);
            youTubeElements.push({"el": el, "videoId": matches[1]});
        }
    });

    container.find("iframe").each(function(idx, iframe) {
        console.log("[YOUTUBE HANDLING] Found iframe: " + iframe);
        var matches = $(iframe).attr("src").match(/youtube.*\/embed\/(\w+)\b/);
        if (matches) {
            console.log("[YOUTUBE HANDLING] iframe is a YouTube video");
            youTubeElements.push({"el": iframe, "videoId": matches[1]});
        }
    });

    for (var ii = 0; ii < youTubeElements.length; ii++) {
        var obj = youTubeElements[ii];
        var template = _.template($("#video-template").text());
        $(obj.el).replaceWith($(template(obj)));
    }
}
