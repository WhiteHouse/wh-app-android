# White House for Android mobile application

A native Android (Java) app designed to fetch, cache, and display
multiple feeds containing articles, photos, and live and on demand
video. These are displayed in a web view. Includes support for push
notifications.

This application is under active development and will continue to be
modified and improved over time.

## Goals

By releasing the source code for this app we hope to empower other
governments and organizations to build and release mobile apps to
engage their own citizens and constituencies. In addition, public
review and contribution to the application's code base will help
strengthen and improve the app.

## Requirements

1. Android 2.2 "Froyo" or higher
2. RSS feeds for content to be aggregated and displayed by the app

## Recommended

1. Android 3.1 "Honeycomb" or higher is required for live video streaming.

## Usage

Mobile developers will be able to configure the application to
retrieve and display content from arbitrary RSS feeds. The developer
will be able to configure the app to receive push
notifications. Placeholder assets may be replaced to customize the
app's look and feel.

### Building the App

The Android SDK for API level 16 is require to build the app.

The White House app uses [ExoPlayer][], release [r1.3.3][]. You need to add the source of the
library into the root directory of wh-app-android yourself, and do

    git checkout r1.3.3

### Code Style

Contributers are expected to adhere to the official [Android
Code Style Guidelines][style].


NOTE: Setting up the application and configuring it for use in your
organization's context requires Android development experience. The
application ships with a similar design to what is used in the White
House for Android mobile application. The application ships with
"white label" placeholder assets that should be replaced by the
developer.

##Roadmap

Have an idea or question about future features for White House for
Android? Let us know by opening a ticket on GitHub, tweeting @WHWeb,
or via our tech feedback form:
http://www.whitehouse.gov/tech/feedback.

##Contributing

Anyone is encouraged to contribute to the project by
[forking][] and submitting
a pull request. (If you are new to GitHub, you might start with a
[basic tutorial][].)
 
By contributing to this project, you grant a world-wide, royalty-free,
perpetual, irrevocable, non-exclusive, transferable license, free of
charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use, copy,
modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished
to do so, subject to the conditions that any appropriate copyright
notices and this permission notice are included in all copies or
substantial portions of the Software.
 
All comments, messages, pull requests, and other submissions received
through official White House pages including this GitHub page are
subject to the Presidential Records Act and may be archived. Learn
more http://WhiteHouse.gov/privacy
 
## License

This project constitutes a work of the United States Government and is
not subject to domestic copyright protection under 17 USC § 105.

However, because the project utilizes code licensed from contributors
and other third parties, it therefore is licensed under the MIT
License.  http://opensource.org/licenses/mit-license.php.  Under that
license, permission is granted free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the conditions that any appropriate copyright notices and this
permission notice are included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.



[ExoPlayer]: https://github.com/google/ExoPlayer
[r1.3.3]: https://github.com/google/ExoPlayer/releases/tag/r1.3.3

[style]: http://source.android.com/source/code-style.html
[forking]: https://help.github.com/articles/fork-a-repo
[basic tutorial]: https://help.github.com/articles/set-up-git
