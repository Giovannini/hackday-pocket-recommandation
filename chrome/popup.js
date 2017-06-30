document.addEventListener('DOMContentLoaded', function() {
  var checkPageButton = document.getElementById('checkPage');
  checkPageButton.addEventListener('click', function() {

    chrome.tabs.getSelected(null, function(tab) {
      d = document;

      console.log(tab.url);

      getInfos(tab.url);

      // var f = d.createElement('ul');
      // var f1 = d.createElement('li');
      // f1.textContent = 'Kévin Laurent';
      //
      // var f2 = d.createElement('li');
      // f2.textContent = 'Hamza Bahassou';
      //
      // f.appendChild(f1);
      // f.appendChild(f2);
      // d.body.appendChild(f);
    });
  }, false);
}, false);

function getInfos(url) {
  const xhr = new XMLHttpRequest();
  xhr.open('POST', 'http://localhost:8080/recommand', true);
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.onreadystatechange = function() {
    if (xhr.readyState === XMLHttpRequest.DONE) {
      console.log(xhr.responseText);
    }
  };
  xhr.send(JSON.stringify({ url }));
}