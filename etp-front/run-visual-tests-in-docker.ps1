param(
    [Parameter(Position=0, Mandatory=$false)]
    [string]$action
)

if ($action -eq 'update') {
    $update = "true"
} else {
    $update = "false"
}

docker build -t etp-front-visual-tests -f visual-tests.Dockerfile .

# Run the visual tests inside a container, pass the flag whether to update image snapshots or not
docker run --name etp-front-visual-tests-container etp-front-visual-tests ./run-visual-tests.sh "$update"
$status = $?

# Delete previous diffs
Remove-Item -Recurse -Force "__snapshots__\__diff_output__"

# Copy the results to local machine from the container
docker cp etp-front-visual-tests-container:/visual-tests/__snapshots__ .

docker rm etp-front-visual-tests-container

# Exit with the code returned by the test runner in the container
exit $status
