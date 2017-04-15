if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'aps-back-phi'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'aps-back-phi'.");
}
this['aps-back-phi'] = function (_, Kotlin) {
  'use strict';
  var println = Kotlin.kotlin.io.println_s8jyv4$;
  function main(args) {
    printSomeShit('Fuck');
    printSomeShit('Shit');
    printSomeShit('Bitch');
  }
  function printSomeShit(shit) {
    println('First ' + shit);
    println('Second ' + shit);
  }
  var package$aps = _.aps || (_.aps = {});
  var package$back = package$aps.back || (package$aps.back = {});
  package$back.main_kand9s$ = main;
  package$back.printSomeShit_61zpoe$ = printSomeShit;
  Kotlin.defineModule('aps-back-phi', _);
  main([]);
  return _;
}(typeof this['aps-back-phi'] === 'undefined' ? {} : this['aps-back-phi'], kotlin);

//@ sourceMappingURL=aps-back-phi.js.map
