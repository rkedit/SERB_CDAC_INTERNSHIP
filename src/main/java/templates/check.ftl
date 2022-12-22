<!doctype html>
<html lang="en">

<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
          integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
          crossorigin="anonymous">

    <title>DESCo :: Defect Estimator for Source Code</title>
    <style>
        td {
            vertical-align: top;
            font-size: 0.9em;
        }
        th {
            font-variant: small-caps;
            font-size: 0.9em;
        }
    </style>
</head>

<body>
<div class="container">
    <h3>DESCo {Defect Estimator for Source Code}</h3>
    <b>Results for file ${context.FileName}</b> [<a href="/">Home</a>]
    <table class="table table-striped table-sm">
        <thead>
        <tr>
            <th>S#</th>
            <th>Scenario</th>
            <th>Result</th>
            <th>Accuracy (0-1)</th>
        </tr>
        </thead>
        <tbody>
            <#if !context.Failed>
                <#list context.Pred as x>
                <tr>
                    <td>${x?index + 1}</td>
                    <td>${x.scenario}</td>
                    <td>${x.result}</td>
                    <td>${x.accuracy} &plusmn; ${x.error}</td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td colspan="4">Could not fetch results. Please retry later.</td>
            </tr>
            </#if>
        </tbody>
    </table>

</div>
</body>

</html>