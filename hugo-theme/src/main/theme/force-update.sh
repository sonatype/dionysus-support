#!/bin/sh
basename=`basename $0`
dirname=`dirname $0`
dirname=`cd "$dirname" && pwd`
cd "$dirname"

#
# HACK: to force out-of-band changes to css and js files; something with node runtime and how
# HACK: brunch works to generate files is not getting picked up by hugo
#

marker=`uuid`
css='static/theme.css'
#css=$(ls static/theme-*.css)
js='static/theme.js'
#js=$(ls static/theme-*.js)

printf '\n/*%s/*' $marker >> "$css"
printf '\n//%s' $marker >> "$js"

# spit out the marker for hugo project to wait on
echo $marker > force-update.marker
