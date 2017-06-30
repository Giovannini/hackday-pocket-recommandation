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
  // const xhr = new XMLHttpRequest();
  // xhr.open('POST', 'http://localhost:8080/recommand', true);
  // xhr.setRequestHeader("Content-Type", "application/json");
  // xhr.onreadystatechange = function() {
  //   if (xhr.readyState === XMLHttpRequest.DONE) {
  //     console.log(xhr.responseText);
  //   }
  // };
  // xhr.send(JSON.stringify({ url }));
  console.log(window.location);

  fetch('http://localhost:8080/recommand', {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Access-Control-Allow-Origin': window.location.origin,
      'Content-Type': 'application/json',
      'Origin': window.location.origin,
      // 'Cache-Control': 'no-cache',
    },
    body: JSON.stringify({ url })
  }).then(res => {
    console.log(res);
    return res.json()
  }, err => console.error(err)).then(
    s => console.log(s),
    e => console.warn(e)
  )
}