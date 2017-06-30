document.addEventListener('DOMContentLoaded', function() {
  const contentDiv = document.getElementById('content');
  const loader = document.getElementById('loader');

  loader.style.display = "block";

  chrome.tabs.getSelected(null, function(tab) {
    d = document;

    console.log(tab.url);
    while (contentDiv.firstChild) {
      contentDiv.removeChild(contentDiv.firstChild);
    }

    getInfos(tab.url).then(response => {
      loader.style.display = "none";
      writeResult(response.entities, d, contentDiv);
    })

  });
}, false);

function getInfos(url) {
  return fetch('http://localhost:8080/recommand', {
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
    return res.json()
  }, err => console.error(err))
}

function writeResult(entities, d, contentDiv) {
  const f = d.createElement('ul');
  entities.map(e => {
    const li = d.createElement('li');
    const relevanceSpan = d.createElement('span');
    const wordSpan = d.createElement('span');
    relevanceSpan.textContent = `${e.relevance}`;
    relevanceSpan.className = "relevance-span";
    wordSpan.textContent = `${e.word}`;
    wordSpan.className = "word-span";
    li.appendChild(wordSpan);
    li.appendChild(relevanceSpan);
    f.appendChild(li);
  });

  contentDiv.appendChild(f);
}