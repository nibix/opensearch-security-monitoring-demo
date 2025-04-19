function parse_privilege_matrix(tag, timestamp, record)
    local raw = record["log"]
    local actions = {}
    local action_positions = {}
    local results = {}

    -- Step 1: Split into lines
    local lines = {}
    for line in raw:gmatch("[^\r\n]+") do
        table.insert(lines, line)
    end

    if #lines < 2 then
        return 0, timestamp, record  -- Not enough lines to process
    end

    -- Step 2: Parse header line to extract action names and positions
    local header = lines[2]
    for pos, action in header:gmatch("()|%s*([^|]+)%s*") do
        table.insert(actions, action)
        table.insert(action_positions, pos)
        results[action] = {}
    end

    -- Step 3: Parse each data line using column positions
    for i = 3, #lines do
        local line = lines[i]
        local idx = line:match("^%s*(.-)%s*|")
        if idx then
            for j, pos in ipairs(action_positions) do
                local next_pos = action_positions[j + 1] or #line + 1
                local value = line:sub(pos, next_pos - 1):match("|%s*([^|%s]*)%s*")
                if value and value ~= "" then
                    local action = actions[j]
                    results[action][idx] = value
                end
            end
        end
    end

    -- Merge results into record
    for action, index_map in pairs(results) do
        record[action] = index_map
    end

    return 1, timestamp, record
end

function trim(s)
    return (s:gsub("^%s*(.-)%s*$", "%1"))
end

function parse_privilege_matrix_by_index_and_action(tag, timestamp, record)
    local raw = record["log"]
    local actions = {}
    local action_positions = {}
    local missing = {}
    local available = {}

    -- Step 1: Split into lines
    local lines = {}
    for line in raw:gmatch("[^\r\n]+") do
        table.insert(lines, line)
    end

    if #lines < 2 then
        return 0, timestamp, record  -- Not enough lines to process
    end

    -- Step 2: Parse header line to extract action names and positions
    local header = lines[2]
    for pos, action in header:gmatch("()|%s*([^|]+)%s*") do
        local cleaned = trim(action)
        table.insert(actions, cleaned)
        table.insert(action_positions, pos)
    end

    -- Step 3: Parse each data line using column positions
    for i = 3, #lines do
        local line = lines[i]
        local idx = line:match("^%s*(.-)%s*|")
        if idx then
            for j, pos in ipairs(action_positions) do
                local next_pos = action_positions[j + 1] or #line + 1
                local value = line:sub(pos, next_pos - 1):match("|%s*([^|%s]*)%s*")
                local action = actions[j]
                if value == "MISSING" then
                    missing[idx] = missing[idx] or {}
                    table.insert(missing[idx], action)
                elseif value == "ok" then
                    available[idx] = available[idx] or {}
                    table.insert(available[idx], action)
                end
            end
        end
    end

    -- Return structured result
    record["missing_privileges"] = missing
    record["available_privileges"] = available
    return 1, timestamp, record
end
