//
// Theme
//

console.log('INIT');

import 'bootstrap';

// include search and jquery plugin it requires
import 'mark.js/dist/jquery.mark.min.js';
import 'search';

document.addEventListener('DOMContentLoaded', () => {
  console.log('LOADED');

  // initialize all tooltips
  $('[data-toggle="tooltip"]').tooltip({
    // delay showing, hiding tooltips slightly
    delay: {show: 1000, hide: 250}
  });

  // custom handling for dropdown button tooltips
  var dropdown = $('.dropdown-tooltip');
  dropdown.tooltip({
    // delay showing, hiding tooltips slightly
    delay: {show: 1000, hide: 250},
    trigger: 'hover'
  });

  // automatically hide tooltip on click
  dropdown.on('click', function() {
    $(this).tooltip('hide');
  });
});