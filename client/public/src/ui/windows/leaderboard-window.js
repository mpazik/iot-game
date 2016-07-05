define(function (require, exports, module) {
    return createUiElement('leaderboard-window', {
        type: 'window',
        properties: {
            requirements: {
                applicationState: Predicates.is('running')
            }
        },
        created: function () {
            this.innerHTML = `
<table>
    <thead>
        <tr><td>Rank</td><td>Nick</td><td>Record</td></tr>
    </thead>
    <tbody></tbody>
</table>            
`;
        },
        attached: function () {
            const table = this.getElementsByTagName('tbody')[0];
            Request.Server.leaderboard().then(function (data) {
                table.innerHTML = data.map(function (rowData) {
                    return `<tr><td>${rowData.position}.</td><td>${rowData.nick}</td><td>${rowData.record}</td>`;
                }).join('\n');
            });
        }
    });
});