import React, { useState, useEffect, useCallback } from 'react';
import './SovereignBrain.css';

// ═══════════════════════════════════════════════════════════════════════════════
// SOVEREIGNBRAIN - VaultMesh Productivity Dashboard
// Alchemical Transformation Engine for Digital Sovereignty
// ═══════════════════════════════════════════════════════════════════════════════

// Utility: Generate cryptographic-style IDs
const generateHexId = () => `0x${Array.from({length: 8}, () =>
  Math.floor(Math.random() * 16).toString(16)).join('')}`;

// Utility: VaultMesh timestamp
const vaultTimestamp = () => {
  const now = new Date();
  return `VM.${now.getFullYear()}.${String(now.getMonth()+1).padStart(2,'0')}.${String(now.getDate()).padStart(2,'0')}.${String(now.getHours()).padStart(2,'0')}${String(now.getMinutes()).padStart(2,'0')}`;
};

// ═══════════════════════════════════════════════════════════════════════════════
// CONSTANTS & CONFIGURATION
// ═══════════════════════════════════════════════════════════════════════════════

const ALCHEMICAL_PHASES = {
  NIGREDO: { name: 'Nigredo', symbol: '☽', color: '#1a1a2e', description: 'Decomposition - Breaking down old structures' },
  ALBEDO: { name: 'Albedo', symbol: '☿', color: '#4a5568', description: 'Purification - Cleansing and clarifying' },
  CITRINITAS: { name: 'Citrinitas', symbol: '☉', color: '#d69e2e', description: 'Awakening - Solar consciousness emerges' },
  RUBEDO: { name: 'Rubedo', symbol: '♃', color: '#c53030', description: 'Completion - The Philosopher\'s Stone achieved' }
};

const VAULTMESH_ORGANS = {
  GOVERNANCE: { name: 'Governance', icon: '⚖️', color: '#6366f1' },
  AUTOMATION: { name: 'Automation', icon: '⚙️', color: '#22c55e' },
  TREASURY: { name: 'Treasury', icon: '🏛️', color: '#f59e0b' },
  FEDERATION: { name: 'Federation', icon: '🌐', color: '#06b6d4' },
  PSI_FIELD: { name: 'Ψ-Field', icon: 'Ψ', color: '#a855f7' },
  INFRASTRUCTURE: { name: 'Infrastructure', icon: '🔧', color: '#ef4444' }
};

const PRIORITY_LEVELS = {
  CRITICAL: { label: 'Critical', color: '#dc2626', weight: 4 },
  HIGH: { label: 'High', color: '#f97316', weight: 3 },
  MEDIUM: { label: 'Medium', color: '#eab308', weight: 2 },
  LOW: { label: 'Low', color: '#22c55e', weight: 1 }
};

const MOOD_STATES = {
  TRANSCENDENT: { emoji: '✨', label: 'Transcendent', value: 5 },
  FOCUSED: { emoji: '🎯', label: 'Focused', value: 4 },
  STABLE: { emoji: '⚖️', label: 'Stable', value: 3 },
  TURBULENT: { emoji: '🌊', label: 'Turbulent', value: 2 },
  DISSOLVING: { emoji: '🌑', label: 'Dissolving', value: 1 }
};

// ═══════════════════════════════════════════════════════════════════════════════
// INITIAL DATA - VaultMesh Operations Context
// ═══════════════════════════════════════════════════════════════════════════════

const INITIAL_TASKS = [
  {
    id: generateHexId(),
    title: 'Phase VII Deployment - Tem Engine Core',
    description: 'Deploy sovereign template engine with cryptographic attestation',
    phase: 'CITRINITAS',
    organ: 'AUTOMATION',
    priority: 'CRITICAL',
    dueDate: '2024-12-15',
    subtasks: [
      { id: generateHexId(), title: 'Finalize WASM compilation pipeline', completed: true },
      { id: generateHexId(), title: 'Integrate ZK-proof verification', completed: false },
      { id: generateHexId(), title: 'Deploy to sovereign infrastructure', completed: false }
    ],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Horizon Europe Funding Application',
    description: 'Complete consortium documentation for digital sovereignty grant',
    phase: 'ALBEDO',
    organ: 'TREASURY',
    priority: 'HIGH',
    dueDate: '2024-12-20',
    subtasks: [
      { id: generateHexId(), title: 'Draft technical architecture section', completed: true },
      { id: generateHexId(), title: 'Gather partner commitments', completed: true },
      { id: generateHexId(), title: 'Budget reconciliation', completed: false }
    ],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'LAWCHAIN Smart Contract Audit',
    description: 'Security review of legal automation contracts',
    phase: 'NIGREDO',
    organ: 'GOVERNANCE',
    priority: 'HIGH',
    dueDate: '2024-12-18',
    subtasks: [
      { id: generateHexId(), title: 'Static analysis with Slither', completed: false },
      { id: generateHexId(), title: 'Formal verification setup', completed: false }
    ],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Federation Node Protocol v2',
    description: 'Implement cross-mesh communication standards',
    phase: 'ALBEDO',
    organ: 'FEDERATION',
    priority: 'MEDIUM',
    dueDate: '2024-12-25',
    subtasks: [],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Ψ-Field Coherence Monitor',
    description: 'Build real-time dashboard for emergent pattern detection',
    phase: 'NIGREDO',
    organ: 'PSI_FIELD',
    priority: 'MEDIUM',
    dueDate: '2025-01-05',
    subtasks: [],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Infrastructure Sovereignty Audit',
    description: 'Verify all systems meet self-hosting requirements',
    phase: 'RUBEDO',
    organ: 'INFRASTRUCTURE',
    priority: 'LOW',
    dueDate: '2025-01-10',
    subtasks: [],
    created: vaultTimestamp()
  }
];

const INITIAL_NOTES = [
  {
    id: generateHexId(),
    title: 'Tem Engine Architecture',
    content: `# Tem Engine - Sovereign Template System

## Core Principles
- **Cryptographic Attestation**: Every template signed with ed25519
- **WASM Isolation**: Sandboxed execution environment
- **Merkle Provenance**: Full audit trail of transformations

## Architecture Layers
1. **Parser Layer**: AST generation from .tem files
2. **Transform Layer**: Alchemical phase transitions
3. **Output Layer**: Multi-target compilation (HTML, PDF, JSON)

## Integration Points
- VaultMesh Governance for access control
- Treasury for metered usage
- Ψ-Field for pattern emergence tracking`,
    organ: 'AUTOMATION',
    tags: ['architecture', 'tem', 'core'],
    created: vaultTimestamp(),
    modified: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Alchemical Computing Manifesto',
    content: `# Alchemical Computing

> "As above, so below; as within, so without"

## The Four Phases in Software

### Nigredo (Blackening)
- Decomposition of legacy systems
- Breaking down monoliths
- Facing technical debt

### Albedo (Whitening)
- Purification through refactoring
- Clean architecture emergence
- Documentation clarity

### Citrinitas (Yellowing)
- Solar consciousness in code
- Self-documenting systems
- Emergent intelligence

### Rubedo (Reddening)
- The Philosopher's Stone
- True digital sovereignty
- Living systems`,
    organ: 'PSI_FIELD',
    tags: ['philosophy', 'alchemy', 'manifesto'],
    created: vaultTimestamp(),
    modified: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Treasury Protocol Specifications',
    content: `# VaultMesh Treasury Protocol

## Token Economics
- **VAULT**: Governance token
- **MESH**: Utility token for compute
- **Bonding Curves**: Autonomous market making

## Smart Contract Architecture
\`\`\`solidity
contract Treasury {
    mapping(address => uint256) public stakes;
    uint256 public totalLocked;

    function deposit(uint256 amount) external;
    function withdraw(uint256 amount) external;
    function vote(bytes32 proposalId) external;
}
\`\`\``,
    organ: 'TREASURY',
    tags: ['treasury', 'smart-contracts', 'tokenomics'],
    created: vaultTimestamp(),
    modified: vaultTimestamp()
  }
];

const INITIAL_JOURNAL = [
  {
    id: generateHexId(),
    date: new Date().toISOString().split('T')[0],
    mood: 'FOCUSED',
    phase: 'CITRINITAS',
    reflection: `Deep work session on Tem Engine. The WASM compilation pipeline is finally stable. Feeling the solar consciousness emerging - code that writes itself, patterns that self-organize.

Key insight: The alchemical phases aren't sequential, they're concurrent. Nigredo in one module while Rubedo completes in another. The mesh breathes.

Tomorrow: Focus on ZK integration. The proof system needs to be elegant, not just functional.`,
    gratitude: ['Stable build after 3 days of debugging', 'Clear architectural vision', 'Strong coffee'],
    intentions: ['Complete ZK-proof integration', 'Review Horizon Europe draft', 'Evening meditation']
  }
];

const INITIAL_BOOKMARKS = [
  {
    id: generateHexId(),
    title: 'WASM Component Model Spec',
    url: 'https://component-model.bytecodealliance.org/',
    organ: 'AUTOMATION',
    tags: ['wasm', 'spec', 'reference'],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Zero Knowledge Proofs - An Introduction',
    url: 'https://zkp.science/',
    organ: 'GOVERNANCE',
    tags: ['zk', 'cryptography', 'learning'],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'Horizon Europe Digital Sovereignty Calls',
    url: 'https://ec.europa.eu/info/funding-tenders/opportunities/portal/',
    organ: 'TREASURY',
    tags: ['funding', 'eu', 'grants'],
    created: vaultTimestamp()
  },
  {
    id: generateHexId(),
    title: 'ActivityPub Specification',
    url: 'https://www.w3.org/TR/activitypub/',
    organ: 'FEDERATION',
    tags: ['federation', 'protocol', 'spec'],
    created: vaultTimestamp()
  }
];

const INITIAL_EVENTS = [
  {
    id: generateHexId(),
    title: 'Horizon Europe Deadline',
    date: '2024-12-20',
    type: 'deadline',
    organ: 'TREASURY'
  },
  {
    id: generateHexId(),
    title: 'Phase VII Launch',
    date: '2024-12-15',
    type: 'milestone',
    organ: 'AUTOMATION'
  },
  {
    id: generateHexId(),
    title: 'Consortium Call',
    date: '2024-12-10',
    type: 'meeting',
    organ: 'FEDERATION'
  },
  {
    id: generateHexId(),
    title: 'Security Audit Review',
    date: '2024-12-18',
    type: 'review',
    organ: 'GOVERNANCE'
  }
];

// ═══════════════════════════════════════════════════════════════════════════════
// COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

// Navigation Component
const Navigation = ({ activeModule, setActiveModule }) => {
  const modules = [
    { id: 'dashboard', label: 'Dashboard', icon: '◈' },
    { id: 'tasks', label: 'Task Matrix', icon: '☰' },
    { id: 'notes', label: 'Knowledge Base', icon: '📜' },
    { id: 'journal', label: 'Alchemical Journal', icon: '🌙' },
    { id: 'calendar', label: 'Calendar', icon: '📅' },
    { id: 'bookmarks', label: 'Bookmarks', icon: '🔖' }
  ];

  return (
    <nav className="sovereign-nav">
      <div className="nav-brand">
        <span className="brand-symbol">◉</span>
        <span className="brand-text">SovereignBrain</span>
        <span className="brand-version">v0.1.0</span>
      </div>
      <ul className="nav-modules">
        {modules.map(module => (
          <li key={module.id}>
            <button
              className={`nav-button ${activeModule === module.id ? 'active' : ''}`}
              onClick={() => setActiveModule(module.id)}
            >
              <span className="nav-icon">{module.icon}</span>
              <span className="nav-label">{module.label}</span>
            </button>
          </li>
        ))}
      </ul>
      <div className="nav-footer">
        <div className="timestamp">{vaultTimestamp()}</div>
        <div className="mesh-status">
          <span className="status-dot online"></span>
          VaultMesh Connected
        </div>
      </div>
    </nav>
  );
};

// Dashboard Component
const Dashboard = ({ tasks, notes, journal, events }) => {
  const tasksByPhase = Object.keys(ALCHEMICAL_PHASES).reduce((acc, phase) => {
    acc[phase] = tasks.filter(t => t.phase === phase).length;
    return acc;
  }, {});

  const tasksByOrgan = Object.keys(VAULTMESH_ORGANS).reduce((acc, organ) => {
    acc[organ] = tasks.filter(t => t.organ === organ).length;
    return acc;
  }, {});

  const totalTasks = tasks.length;
  const criticalTasks = tasks.filter(t => t.priority === 'CRITICAL').length;
  const todayEntry = journal[0];

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Command Center</h1>
        <p className="header-subtitle">VaultMesh Operational Status</p>
      </header>

      <div className="dashboard-grid">
        {/* Stats Cards */}
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-value">{totalTasks}</div>
            <div className="stat-label">Active Operations</div>
          </div>
          <div className="stat-card critical">
            <div className="stat-value">{criticalTasks}</div>
            <div className="stat-label">Critical Priority</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{notes.length}</div>
            <div className="stat-label">Knowledge Entries</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{events.length}</div>
            <div className="stat-label">Upcoming Events</div>
          </div>
        </div>

        {/* Phase Distribution */}
        <div className="dashboard-card phase-distribution">
          <h3>Alchemical Phase Distribution</h3>
          <div className="phase-bars">
            {Object.entries(ALCHEMICAL_PHASES).map(([key, phase]) => (
              <div key={key} className="phase-bar-container">
                <div className="phase-bar-label">
                  <span className="phase-symbol">{phase.symbol}</span>
                  <span>{phase.name}</span>
                </div>
                <div className="phase-bar-track">
                  <div
                    className="phase-bar-fill"
                    style={{
                      width: `${(tasksByPhase[key] / Math.max(totalTasks, 1)) * 100}%`,
                      backgroundColor: phase.color
                    }}
                  />
                </div>
                <span className="phase-bar-count">{tasksByPhase[key]}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Six Organs Status */}
        <div className="dashboard-card organs-status">
          <h3>Six Organs Status</h3>
          <div className="organs-grid">
            {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
              <div key={key} className="organ-card" style={{ borderColor: organ.color }}>
                <div className="organ-icon" style={{ color: organ.color }}>{organ.icon}</div>
                <div className="organ-name">{organ.name}</div>
                <div className="organ-tasks">{tasksByOrgan[key]} tasks</div>
              </div>
            ))}
          </div>
        </div>

        {/* Today's Mood */}
        {todayEntry && (
          <div className="dashboard-card mood-card">
            <h3>Current State</h3>
            <div className="mood-display">
              <span className="mood-emoji">{MOOD_STATES[todayEntry.mood]?.emoji}</span>
              <span className="mood-label">{MOOD_STATES[todayEntry.mood]?.label}</span>
            </div>
            <div className="current-phase">
              <span>Active Phase: </span>
              <span className="phase-badge" style={{ backgroundColor: ALCHEMICAL_PHASES[todayEntry.phase]?.color }}>
                {ALCHEMICAL_PHASES[todayEntry.phase]?.symbol} {ALCHEMICAL_PHASES[todayEntry.phase]?.name}
              </span>
            </div>
          </div>
        )}

        {/* Upcoming Events */}
        <div className="dashboard-card events-preview">
          <h3>Upcoming Events</h3>
          <ul className="events-list">
            {events.slice(0, 4).map(event => (
              <li key={event.id} className="event-item">
                <span className="event-date">{event.date}</span>
                <span className="event-title">{event.title}</span>
                <span className="event-type" data-type={event.type}>{event.type}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};

// Task Matrix Component (Kanban by Alchemical Phase)
const TaskMatrix = ({ tasks, setTasks }) => {
  const [selectedTask, setSelectedTask] = useState(null);
  const [showAddTask, setShowAddTask] = useState(false);
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    phase: 'NIGREDO',
    organ: 'AUTOMATION',
    priority: 'MEDIUM',
    dueDate: ''
  });

  const moveTask = (taskId, newPhase) => {
    setTasks(tasks.map(t =>
      t.id === taskId ? { ...t, phase: newPhase } : t
    ));
  };

  const toggleSubtask = (taskId, subtaskId) => {
    setTasks(tasks.map(t => {
      if (t.id === taskId) {
        return {
          ...t,
          subtasks: t.subtasks.map(st =>
            st.id === subtaskId ? { ...st, completed: !st.completed } : st
          )
        };
      }
      return t;
    }));
  };

  const addTask = () => {
    if (!newTask.title.trim()) return;

    const task = {
      id: generateHexId(),
      ...newTask,
      subtasks: [],
      created: vaultTimestamp()
    };

    setTasks([...tasks, task]);
    setNewTask({
      title: '',
      description: '',
      phase: 'NIGREDO',
      organ: 'AUTOMATION',
      priority: 'MEDIUM',
      dueDate: ''
    });
    setShowAddTask(false);
  };

  const deleteTask = (taskId) => {
    setTasks(tasks.filter(t => t.id !== taskId));
    setSelectedTask(null);
  };

  return (
    <div className="task-matrix-container">
      <header className="matrix-header">
        <h1>Task Matrix</h1>
        <p className="header-subtitle">Alchemical Transformation Pipeline</p>
        <button className="add-task-btn" onClick={() => setShowAddTask(true)}>
          + New Task
        </button>
      </header>

      <div className="kanban-board">
        {Object.entries(ALCHEMICAL_PHASES).map(([phaseKey, phase]) => (
          <div key={phaseKey} className="kanban-column" style={{ borderTopColor: phase.color }}>
            <div className="column-header">
              <span className="phase-symbol">{phase.symbol}</span>
              <span className="phase-name">{phase.name}</span>
              <span className="task-count">{tasks.filter(t => t.phase === phaseKey).length}</span>
            </div>
            <div className="column-description">{phase.description}</div>
            <div className="task-list">
              {tasks.filter(t => t.phase === phaseKey).map(task => (
                <div
                  key={task.id}
                  className="task-card"
                  onClick={() => setSelectedTask(task)}
                  style={{ borderLeftColor: PRIORITY_LEVELS[task.priority]?.color }}
                >
                  <div className="task-header">
                    <span className="task-organ" style={{ color: VAULTMESH_ORGANS[task.organ]?.color }}>
                      {VAULTMESH_ORGANS[task.organ]?.icon}
                    </span>
                    <span className="task-priority" style={{ color: PRIORITY_LEVELS[task.priority]?.color }}>
                      {task.priority}
                    </span>
                  </div>
                  <h4 className="task-title">{task.title}</h4>
                  {task.dueDate && (
                    <div className="task-due">Due: {task.dueDate}</div>
                  )}
                  {task.subtasks.length > 0 && (
                    <div className="subtask-progress">
                      {task.subtasks.filter(st => st.completed).length}/{task.subtasks.length} subtasks
                    </div>
                  )}
                  <div className="task-id">{task.id}</div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Task Detail Modal */}
      {selectedTask && (
        <div className="modal-overlay" onClick={() => setSelectedTask(null)}>
          <div className="modal-content task-detail-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelectedTask(null)}>×</button>
            <div className="task-detail-header">
              <span className="task-organ-badge" style={{ backgroundColor: VAULTMESH_ORGANS[selectedTask.organ]?.color }}>
                {VAULTMESH_ORGANS[selectedTask.organ]?.icon} {VAULTMESH_ORGANS[selectedTask.organ]?.name}
              </span>
              <span className="task-priority-badge" style={{ backgroundColor: PRIORITY_LEVELS[selectedTask.priority]?.color }}>
                {selectedTask.priority}
              </span>
            </div>
            <h2>{selectedTask.title}</h2>
            <p className="task-description">{selectedTask.description}</p>

            <div className="task-meta">
              <div><strong>ID:</strong> {selectedTask.id}</div>
              <div><strong>Created:</strong> {selectedTask.created}</div>
              <div><strong>Due:</strong> {selectedTask.dueDate || 'Not set'}</div>
            </div>

            <div className="phase-selector">
              <label>Move to Phase:</label>
              <div className="phase-buttons">
                {Object.entries(ALCHEMICAL_PHASES).map(([key, phase]) => (
                  <button
                    key={key}
                    className={`phase-btn ${selectedTask.phase === key ? 'active' : ''}`}
                    style={{ backgroundColor: phase.color }}
                    onClick={() => {
                      moveTask(selectedTask.id, key);
                      setSelectedTask({ ...selectedTask, phase: key });
                    }}
                  >
                    {phase.symbol} {phase.name}
                  </button>
                ))}
              </div>
            </div>

            {selectedTask.subtasks.length > 0 && (
              <div className="subtasks-section">
                <h4>Subtasks</h4>
                <ul className="subtasks-list">
                  {selectedTask.subtasks.map(st => (
                    <li key={st.id} className={st.completed ? 'completed' : ''}>
                      <label>
                        <input
                          type="checkbox"
                          checked={st.completed}
                          onChange={() => {
                            toggleSubtask(selectedTask.id, st.id);
                            setSelectedTask({
                              ...selectedTask,
                              subtasks: selectedTask.subtasks.map(s =>
                                s.id === st.id ? { ...s, completed: !s.completed } : s
                              )
                            });
                          }}
                        />
                        {st.title}
                      </label>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <button className="delete-task-btn" onClick={() => deleteTask(selectedTask.id)}>
              Delete Task
            </button>
          </div>
        </div>
      )}

      {/* Add Task Modal */}
      {showAddTask && (
        <div className="modal-overlay" onClick={() => setShowAddTask(false)}>
          <div className="modal-content add-task-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setShowAddTask(false)}>×</button>
            <h2>Create New Task</h2>

            <div className="form-group">
              <label>Title</label>
              <input
                type="text"
                value={newTask.title}
                onChange={e => setNewTask({ ...newTask, title: e.target.value })}
                placeholder="Task title..."
              />
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                value={newTask.description}
                onChange={e => setNewTask({ ...newTask, description: e.target.value })}
                placeholder="Task description..."
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Phase</label>
                <select
                  value={newTask.phase}
                  onChange={e => setNewTask({ ...newTask, phase: e.target.value })}
                >
                  {Object.entries(ALCHEMICAL_PHASES).map(([key, phase]) => (
                    <option key={key} value={key}>{phase.symbol} {phase.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Organ</label>
                <select
                  value={newTask.organ}
                  onChange={e => setNewTask({ ...newTask, organ: e.target.value })}
                >
                  {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
                    <option key={key} value={key}>{organ.icon} {organ.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Priority</label>
                <select
                  value={newTask.priority}
                  onChange={e => setNewTask({ ...newTask, priority: e.target.value })}
                >
                  {Object.entries(PRIORITY_LEVELS).map(([key, level]) => (
                    <option key={key} value={key}>{level.label}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Due Date</label>
                <input
                  type="date"
                  value={newTask.dueDate}
                  onChange={e => setNewTask({ ...newTask, dueDate: e.target.value })}
                />
              </div>
            </div>

            <button className="submit-btn" onClick={addTask}>Create Task</button>
          </div>
        </div>
      )}
    </div>
  );
};

// Knowledge Base Component
const KnowledgeBase = ({ notes, setNotes }) => {
  const [selectedNote, setSelectedNote] = useState(null);
  const [filterOrgan, setFilterOrgan] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState('');
  const [showAddNote, setShowAddNote] = useState(false);
  const [newNote, setNewNote] = useState({
    title: '',
    content: '',
    organ: 'AUTOMATION',
    tags: ''
  });

  const filteredNotes = notes.filter(note => {
    const matchesOrgan = filterOrgan === 'ALL' || note.organ === filterOrgan;
    const matchesSearch = searchQuery === '' ||
      note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      note.content.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesOrgan && matchesSearch;
  });

  const addNote = () => {
    if (!newNote.title.trim()) return;

    const note = {
      id: generateHexId(),
      title: newNote.title,
      content: newNote.content,
      organ: newNote.organ,
      tags: newNote.tags.split(',').map(t => t.trim()).filter(t => t),
      created: vaultTimestamp(),
      modified: vaultTimestamp()
    };

    setNotes([note, ...notes]);
    setNewNote({ title: '', content: '', organ: 'AUTOMATION', tags: '' });
    setShowAddNote(false);
  };

  const saveNote = () => {
    setNotes(notes.map(n =>
      n.id === selectedNote.id
        ? { ...n, content: editContent, modified: vaultTimestamp() }
        : n
    ));
    setSelectedNote({ ...selectedNote, content: editContent });
    setIsEditing(false);
  };

  const deleteNote = (noteId) => {
    setNotes(notes.filter(n => n.id !== noteId));
    setSelectedNote(null);
  };

  return (
    <div className="knowledge-base-container">
      <header className="kb-header">
        <h1>Knowledge Base</h1>
        <p className="header-subtitle">VaultMesh Intelligence Repository</p>
      </header>

      <div className="kb-toolbar">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search knowledge..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="organ-filter">
          <select value={filterOrgan} onChange={e => setFilterOrgan(e.target.value)}>
            <option value="ALL">All Organs</option>
            {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
              <option key={key} value={key}>{organ.icon} {organ.name}</option>
            ))}
          </select>
        </div>
        <button className="add-note-btn" onClick={() => setShowAddNote(true)}>
          + New Note
        </button>
      </div>

      <div className="kb-content">
        <div className="notes-list">
          {filteredNotes.map(note => (
            <div
              key={note.id}
              className={`note-card ${selectedNote?.id === note.id ? 'selected' : ''}`}
              onClick={() => {
                setSelectedNote(note);
                setEditContent(note.content);
                setIsEditing(false);
              }}
            >
              <div className="note-organ" style={{ color: VAULTMESH_ORGANS[note.organ]?.color }}>
                {VAULTMESH_ORGANS[note.organ]?.icon} {VAULTMESH_ORGANS[note.organ]?.name}
              </div>
              <h4 className="note-title">{note.title}</h4>
              <div className="note-tags">
                {note.tags.map(tag => (
                  <span key={tag} className="tag">#{tag}</span>
                ))}
              </div>
              <div className="note-meta">{note.modified}</div>
            </div>
          ))}
        </div>

        <div className="note-viewer">
          {selectedNote ? (
            <>
              <div className="viewer-header">
                <h2>{selectedNote.title}</h2>
                <div className="viewer-actions">
                  {isEditing ? (
                    <>
                      <button onClick={saveNote}>Save</button>
                      <button onClick={() => setIsEditing(false)}>Cancel</button>
                    </>
                  ) : (
                    <>
                      <button onClick={() => setIsEditing(true)}>Edit</button>
                      <button onClick={() => deleteNote(selectedNote.id)}>Delete</button>
                    </>
                  )}
                </div>
              </div>
              <div className="viewer-meta">
                <span className="note-id">{selectedNote.id}</span>
                <span>Created: {selectedNote.created}</span>
                <span>Modified: {selectedNote.modified}</span>
              </div>
              {isEditing ? (
                <textarea
                  className="note-editor"
                  value={editContent}
                  onChange={e => setEditContent(e.target.value)}
                />
              ) : (
                <div className="note-content">
                  <pre>{selectedNote.content}</pre>
                </div>
              )}
            </>
          ) : (
            <div className="no-selection">
              <p>Select a note to view its contents</p>
            </div>
          )}
        </div>
      </div>

      {/* Add Note Modal */}
      {showAddNote && (
        <div className="modal-overlay" onClick={() => setShowAddNote(false)}>
          <div className="modal-content add-note-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setShowAddNote(false)}>×</button>
            <h2>Create New Note</h2>

            <div className="form-group">
              <label>Title</label>
              <input
                type="text"
                value={newNote.title}
                onChange={e => setNewNote({ ...newNote, title: e.target.value })}
                placeholder="Note title..."
              />
            </div>

            <div className="form-group">
              <label>Organ</label>
              <select
                value={newNote.organ}
                onChange={e => setNewNote({ ...newNote, organ: e.target.value })}
              >
                {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
                  <option key={key} value={key}>{organ.icon} {organ.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Tags (comma-separated)</label>
              <input
                type="text"
                value={newNote.tags}
                onChange={e => setNewNote({ ...newNote, tags: e.target.value })}
                placeholder="tag1, tag2, tag3..."
              />
            </div>

            <div className="form-group">
              <label>Content (Markdown supported)</label>
              <textarea
                value={newNote.content}
                onChange={e => setNewNote({ ...newNote, content: e.target.value })}
                placeholder="Write your note in markdown..."
                rows={10}
              />
            </div>

            <button className="submit-btn" onClick={addNote}>Create Note</button>
          </div>
        </div>
      )}
    </div>
  );
};

// Alchemical Journal Component
const AlchemicalJournal = ({ journal, setJournal }) => {
  const [selectedEntry, setSelectedEntry] = useState(journal[0] || null);
  const [isEditing, setIsEditing] = useState(false);
  const [showAddEntry, setShowAddEntry] = useState(false);
  const [newEntry, setNewEntry] = useState({
    mood: 'STABLE',
    phase: 'ALBEDO',
    reflection: '',
    gratitude: '',
    intentions: ''
  });

  const addEntry = () => {
    const entry = {
      id: generateHexId(),
      date: new Date().toISOString().split('T')[0],
      mood: newEntry.mood,
      phase: newEntry.phase,
      reflection: newEntry.reflection,
      gratitude: newEntry.gratitude.split('\n').filter(g => g.trim()),
      intentions: newEntry.intentions.split('\n').filter(i => i.trim())
    };

    setJournal([entry, ...journal]);
    setSelectedEntry(entry);
    setNewEntry({
      mood: 'STABLE',
      phase: 'ALBEDO',
      reflection: '',
      gratitude: '',
      intentions: ''
    });
    setShowAddEntry(false);
  };

  return (
    <div className="journal-container">
      <header className="journal-header">
        <h1>Alchemical Journal</h1>
        <p className="header-subtitle">Daily Transmutation Records</p>
        <button className="add-entry-btn" onClick={() => setShowAddEntry(true)}>
          + New Entry
        </button>
      </header>

      <div className="journal-content">
        <div className="entries-list">
          {journal.map(entry => (
            <div
              key={entry.id}
              className={`entry-card ${selectedEntry?.id === entry.id ? 'selected' : ''}`}
              onClick={() => setSelectedEntry(entry)}
            >
              <div className="entry-date">{entry.date}</div>
              <div className="entry-mood">
                <span className="mood-emoji">{MOOD_STATES[entry.mood]?.emoji}</span>
                <span>{MOOD_STATES[entry.mood]?.label}</span>
              </div>
              <div className="entry-phase">
                <span style={{ color: ALCHEMICAL_PHASES[entry.phase]?.color }}>
                  {ALCHEMICAL_PHASES[entry.phase]?.symbol}
                </span>
                {ALCHEMICAL_PHASES[entry.phase]?.name}
              </div>
            </div>
          ))}
        </div>

        <div className="entry-viewer">
          {selectedEntry ? (
            <>
              <div className="viewer-header">
                <div className="entry-title">
                  <span className="mood-large">{MOOD_STATES[selectedEntry.mood]?.emoji}</span>
                  <div>
                    <h2>{selectedEntry.date}</h2>
                    <div className="entry-state">
                      {MOOD_STATES[selectedEntry.mood]?.label} •
                      <span style={{ color: ALCHEMICAL_PHASES[selectedEntry.phase]?.color }}>
                        {' '}{ALCHEMICAL_PHASES[selectedEntry.phase]?.symbol} {ALCHEMICAL_PHASES[selectedEntry.phase]?.name}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="reflection-section">
                <h3>Reflection</h3>
                <p>{selectedEntry.reflection}</p>
              </div>

              <div className="gratitude-section">
                <h3>Gratitude</h3>
                <ul>
                  {selectedEntry.gratitude.map((g, i) => (
                    <li key={i}>{g}</li>
                  ))}
                </ul>
              </div>

              <div className="intentions-section">
                <h3>Intentions</h3>
                <ul>
                  {selectedEntry.intentions.map((intent, i) => (
                    <li key={i}>{intent}</li>
                  ))}
                </ul>
              </div>
            </>
          ) : (
            <div className="no-selection">
              <p>Select an entry or create a new one</p>
            </div>
          )}
        </div>
      </div>

      {/* Add Entry Modal */}
      {showAddEntry && (
        <div className="modal-overlay" onClick={() => setShowAddEntry(false)}>
          <div className="modal-content add-entry-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setShowAddEntry(false)}>×</button>
            <h2>New Journal Entry</h2>
            <p className="modal-date">{new Date().toISOString().split('T')[0]}</p>

            <div className="form-row">
              <div className="form-group">
                <label>Current Mood</label>
                <div className="mood-selector">
                  {Object.entries(MOOD_STATES).map(([key, mood]) => (
                    <button
                      key={key}
                      className={`mood-btn ${newEntry.mood === key ? 'selected' : ''}`}
                      onClick={() => setNewEntry({ ...newEntry, mood: key })}
                      title={mood.label}
                    >
                      {mood.emoji}
                    </button>
                  ))}
                </div>
              </div>

              <div className="form-group">
                <label>Active Phase</label>
                <select
                  value={newEntry.phase}
                  onChange={e => setNewEntry({ ...newEntry, phase: e.target.value })}
                >
                  {Object.entries(ALCHEMICAL_PHASES).map(([key, phase]) => (
                    <option key={key} value={key}>{phase.symbol} {phase.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>Reflection</label>
              <textarea
                value={newEntry.reflection}
                onChange={e => setNewEntry({ ...newEntry, reflection: e.target.value })}
                placeholder="What transmutations occurred today? What patterns emerged?"
                rows={6}
              />
            </div>

            <div className="form-group">
              <label>Gratitude (one per line)</label>
              <textarea
                value={newEntry.gratitude}
                onChange={e => setNewEntry({ ...newEntry, gratitude: e.target.value })}
                placeholder="What are you grateful for?"
                rows={3}
              />
            </div>

            <div className="form-group">
              <label>Intentions (one per line)</label>
              <textarea
                value={newEntry.intentions}
                onChange={e => setNewEntry({ ...newEntry, intentions: e.target.value })}
                placeholder="What do you intend to manifest?"
                rows={3}
              />
            </div>

            <button className="submit-btn" onClick={addEntry}>Save Entry</button>
          </div>
        </div>
      )}
    </div>
  );
};

// Calendar Component
const Calendar = ({ events, setEvents, tasks }) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(null);
  const [showAddEvent, setShowAddEvent] = useState(false);
  const [newEvent, setNewEvent] = useState({
    title: '',
    date: '',
    type: 'meeting',
    organ: 'AUTOMATION'
  });

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];

  const days = [];
  for (let i = 0; i < firstDay; i++) {
    days.push({ day: null, events: [], tasks: [] });
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const dayEvents = events.filter(e => e.date === dateStr);
    const dayTasks = tasks.filter(t => t.dueDate === dateStr);
    days.push({ day, date: dateStr, events: dayEvents, tasks: dayTasks });
  }

  const prevMonth = () => setCurrentDate(new Date(year, month - 1, 1));
  const nextMonth = () => setCurrentDate(new Date(year, month + 1, 1));

  const addEvent = () => {
    if (!newEvent.title.trim() || !newEvent.date) return;

    const event = {
      id: generateHexId(),
      ...newEvent
    };

    setEvents([...events, event]);
    setNewEvent({ title: '', date: '', type: 'meeting', organ: 'AUTOMATION' });
    setShowAddEvent(false);
  };

  return (
    <div className="calendar-container">
      <header className="calendar-header">
        <h1>Calendar</h1>
        <p className="header-subtitle">Temporal Coordination Matrix</p>
      </header>

      <div className="calendar-nav">
        <button onClick={prevMonth}>&lt; Prev</button>
        <h2>{monthNames[month]} {year}</h2>
        <button onClick={nextMonth}>Next &gt;</button>
        <button className="add-event-btn" onClick={() => setShowAddEvent(true)}>
          + Add Event
        </button>
      </div>

      <div className="calendar-grid">
        <div className="weekday-header">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
            <div key={day} className="weekday">{day}</div>
          ))}
        </div>
        <div className="days-grid">
          {days.map((dayData, idx) => (
            <div
              key={idx}
              className={`day-cell ${dayData.day ? '' : 'empty'} ${
                dayData.date === new Date().toISOString().split('T')[0] ? 'today' : ''
              } ${selectedDate === dayData.date ? 'selected' : ''}`}
              onClick={() => dayData.day && setSelectedDate(dayData.date)}
            >
              {dayData.day && (
                <>
                  <span className="day-number">{dayData.day}</span>
                  <div className="day-indicators">
                    {dayData.events.map(e => (
                      <div
                        key={e.id}
                        className="event-indicator"
                        style={{ backgroundColor: VAULTMESH_ORGANS[e.organ]?.color }}
                        title={e.title}
                      />
                    ))}
                    {dayData.tasks.map(t => (
                      <div
                        key={t.id}
                        className="task-indicator"
                        style={{ backgroundColor: PRIORITY_LEVELS[t.priority]?.color }}
                        title={t.title}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
      </div>

      {selectedDate && (
        <div className="day-detail">
          <h3>{selectedDate}</h3>
          <div className="day-items">
            {events.filter(e => e.date === selectedDate).map(event => (
              <div key={event.id} className="day-event">
                <span className="event-type" data-type={event.type}>{event.type}</span>
                <span className="event-title">{event.title}</span>
                <span className="event-organ" style={{ color: VAULTMESH_ORGANS[event.organ]?.color }}>
                  {VAULTMESH_ORGANS[event.organ]?.icon}
                </span>
              </div>
            ))}
            {tasks.filter(t => t.dueDate === selectedDate).map(task => (
              <div key={task.id} className="day-task">
                <span className="task-phase" style={{ color: ALCHEMICAL_PHASES[task.phase]?.color }}>
                  {ALCHEMICAL_PHASES[task.phase]?.symbol}
                </span>
                <span className="task-title">{task.title}</span>
                <span className="task-priority" style={{ color: PRIORITY_LEVELS[task.priority]?.color }}>
                  {task.priority}
                </span>
              </div>
            ))}
            {events.filter(e => e.date === selectedDate).length === 0 &&
             tasks.filter(t => t.dueDate === selectedDate).length === 0 && (
              <p className="no-items">No events or tasks for this day</p>
            )}
          </div>
        </div>
      )}

      {/* Add Event Modal */}
      {showAddEvent && (
        <div className="modal-overlay" onClick={() => setShowAddEvent(false)}>
          <div className="modal-content add-event-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setShowAddEvent(false)}>×</button>
            <h2>Add Event</h2>

            <div className="form-group">
              <label>Title</label>
              <input
                type="text"
                value={newEvent.title}
                onChange={e => setNewEvent({ ...newEvent, title: e.target.value })}
                placeholder="Event title..."
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Date</label>
                <input
                  type="date"
                  value={newEvent.date}
                  onChange={e => setNewEvent({ ...newEvent, date: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Type</label>
                <select
                  value={newEvent.type}
                  onChange={e => setNewEvent({ ...newEvent, type: e.target.value })}
                >
                  <option value="meeting">Meeting</option>
                  <option value="deadline">Deadline</option>
                  <option value="milestone">Milestone</option>
                  <option value="review">Review</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>Organ</label>
              <select
                value={newEvent.organ}
                onChange={e => setNewEvent({ ...newEvent, organ: e.target.value })}
              >
                {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
                  <option key={key} value={key}>{organ.icon} {organ.name}</option>
                ))}
              </select>
            </div>

            <button className="submit-btn" onClick={addEvent}>Add Event</button>
          </div>
        </div>
      )}
    </div>
  );
};

// Bookmarks Component
const Bookmarks = ({ bookmarks, setBookmarks }) => {
  const [filterOrgan, setFilterOrgan] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddBookmark, setShowAddBookmark] = useState(false);
  const [newBookmark, setNewBookmark] = useState({
    title: '',
    url: '',
    organ: 'AUTOMATION',
    tags: ''
  });

  const filteredBookmarks = bookmarks.filter(bm => {
    const matchesOrgan = filterOrgan === 'ALL' || bm.organ === filterOrgan;
    const matchesSearch = searchQuery === '' ||
      bm.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      bm.url.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesOrgan && matchesSearch;
  });

  const addBookmark = () => {
    if (!newBookmark.title.trim() || !newBookmark.url.trim()) return;

    const bookmark = {
      id: generateHexId(),
      title: newBookmark.title,
      url: newBookmark.url,
      organ: newBookmark.organ,
      tags: newBookmark.tags.split(',').map(t => t.trim()).filter(t => t),
      created: vaultTimestamp()
    };

    setBookmarks([bookmark, ...bookmarks]);
    setNewBookmark({ title: '', url: '', organ: 'AUTOMATION', tags: '' });
    setShowAddBookmark(false);
  };

  const deleteBookmark = (id) => {
    setBookmarks(bookmarks.filter(b => b.id !== id));
  };

  return (
    <div className="bookmarks-container">
      <header className="bookmarks-header">
        <h1>Bookmarks</h1>
        <p className="header-subtitle">Research & Reference Repository</p>
      </header>

      <div className="bookmarks-toolbar">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search bookmarks..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="organ-filter">
          <select value={filterOrgan} onChange={e => setFilterOrgan(e.target.value)}>
            <option value="ALL">All Organs</option>
            {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
              <option key={key} value={key}>{organ.icon} {organ.name}</option>
            ))}
          </select>
        </div>
        <button className="add-bookmark-btn" onClick={() => setShowAddBookmark(true)}>
          + Add Bookmark
        </button>
      </div>

      <div className="bookmarks-grid">
        {filteredBookmarks.map(bookmark => (
          <div key={bookmark.id} className="bookmark-card">
            <div className="bookmark-organ" style={{ color: VAULTMESH_ORGANS[bookmark.organ]?.color }}>
              {VAULTMESH_ORGANS[bookmark.organ]?.icon} {VAULTMESH_ORGANS[bookmark.organ]?.name}
            </div>
            <h4 className="bookmark-title">{bookmark.title}</h4>
            <a href={bookmark.url} target="_blank" rel="noopener noreferrer" className="bookmark-url">
              {bookmark.url}
            </a>
            <div className="bookmark-tags">
              {bookmark.tags.map(tag => (
                <span key={tag} className="tag">#{tag}</span>
              ))}
            </div>
            <div className="bookmark-footer">
              <span className="bookmark-id">{bookmark.id}</span>
              <button className="delete-btn" onClick={() => deleteBookmark(bookmark.id)}>×</button>
            </div>
          </div>
        ))}
      </div>

      {/* Add Bookmark Modal */}
      {showAddBookmark && (
        <div className="modal-overlay" onClick={() => setShowAddBookmark(false)}>
          <div className="modal-content add-bookmark-modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setShowAddBookmark(false)}>×</button>
            <h2>Add Bookmark</h2>

            <div className="form-group">
              <label>Title</label>
              <input
                type="text"
                value={newBookmark.title}
                onChange={e => setNewBookmark({ ...newBookmark, title: e.target.value })}
                placeholder="Bookmark title..."
              />
            </div>

            <div className="form-group">
              <label>URL</label>
              <input
                type="url"
                value={newBookmark.url}
                onChange={e => setNewBookmark({ ...newBookmark, url: e.target.value })}
                placeholder="https://..."
              />
            </div>

            <div className="form-group">
              <label>Organ</label>
              <select
                value={newBookmark.organ}
                onChange={e => setNewBookmark({ ...newBookmark, organ: e.target.value })}
              >
                {Object.entries(VAULTMESH_ORGANS).map(([key, organ]) => (
                  <option key={key} value={key}>{organ.icon} {organ.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Tags (comma-separated)</label>
              <input
                type="text"
                value={newBookmark.tags}
                onChange={e => setNewBookmark({ ...newBookmark, tags: e.target.value })}
                placeholder="tag1, tag2, tag3..."
              />
            </div>

            <button className="submit-btn" onClick={addBookmark}>Add Bookmark</button>
          </div>
        </div>
      )}
    </div>
  );
};

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN APP COMPONENT
// ═══════════════════════════════════════════════════════════════════════════════

const SovereignBrain = () => {
  const [activeModule, setActiveModule] = useState('dashboard');
  const [tasks, setTasks] = useState(INITIAL_TASKS);
  const [notes, setNotes] = useState(INITIAL_NOTES);
  const [journal, setJournal] = useState(INITIAL_JOURNAL);
  const [bookmarks, setBookmarks] = useState(INITIAL_BOOKMARKS);
  const [events, setEvents] = useState(INITIAL_EVENTS);

  // Persist state to localStorage
  useEffect(() => {
    const saved = localStorage.getItem('sovereignBrainState');
    if (saved) {
      try {
        const state = JSON.parse(saved);
        if (state.tasks) setTasks(state.tasks);
        if (state.notes) setNotes(state.notes);
        if (state.journal) setJournal(state.journal);
        if (state.bookmarks) setBookmarks(state.bookmarks);
        if (state.events) setEvents(state.events);
      } catch (e) {
        console.error('Failed to load saved state:', e);
      }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('sovereignBrainState', JSON.stringify({
      tasks, notes, journal, bookmarks, events
    }));
  }, [tasks, notes, journal, bookmarks, events]);

  const renderModule = () => {
    switch (activeModule) {
      case 'dashboard':
        return <Dashboard tasks={tasks} notes={notes} journal={journal} events={events} />;
      case 'tasks':
        return <TaskMatrix tasks={tasks} setTasks={setTasks} />;
      case 'notes':
        return <KnowledgeBase notes={notes} setNotes={setNotes} />;
      case 'journal':
        return <AlchemicalJournal journal={journal} setJournal={setJournal} />;
      case 'calendar':
        return <Calendar events={events} setEvents={setEvents} tasks={tasks} />;
      case 'bookmarks':
        return <Bookmarks bookmarks={bookmarks} setBookmarks={setBookmarks} />;
      default:
        return <Dashboard tasks={tasks} notes={notes} journal={journal} events={events} />;
    }
  };

  return (
    <div className="sovereign-brain">
      <Navigation activeModule={activeModule} setActiveModule={setActiveModule} />
      <main className="main-content">
        {renderModule()}
      </main>
    </div>
  );
};

export default SovereignBrain;
